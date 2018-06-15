package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.model.EavDbSchema;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.UsciException;
import kz.bsbnb.usci.model.eav.base.BaseContainer;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import kz.bsbnb.usci.model.eav.base.BaseValue;
import kz.bsbnb.usci.model.eav.meta.*;
import kz.bsbnb.usci.util.Converter;
import oracle.jdbc.driver.OracleConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BaseEntityApplyServiceImpl implements BaseEntityApplyService {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorImpl.class);

    private final EavHubService eavHubService;
    private final BaseEntityService baseEntityService;
    private final BaseEntityLoadService baseEntityLoadService;
    private final NamedParameterJdbcTemplate npJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public BaseEntityApplyServiceImpl(EavHubService eavHubService,
                                      BaseEntityService baseEntityService,
                                      BaseEntityLoadService baseEntityLoadService,
                                      NamedParameterJdbcTemplate npJdbcTemplate,
                                      JdbcTemplate jdbcTemplate) {
        this.eavHubService = eavHubService;
        this.baseEntityService = baseEntityService;
        this.baseEntityLoadService = baseEntityLoadService;
        this.npJdbcTemplate = npJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BaseEntity apply(long respondentId, final BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager) {
        BaseEntity baseEntityApplied;

        // новые сущности или сущности не имеющие ключевые атрибуты
        if (baseEntitySaving.getId() == null || !baseEntitySaving.getMetaClass().isSearchable())
            baseEntityApplied = applyBaseEntityBasic(respondentId, baseEntitySaving, baseEntityManager);
        else {
            if (baseEntityLoaded == null) {
                LocalDate reportDateSaving = baseEntitySaving.getReportDate();

                // получение максимальной отчетной даты из прошедших периодов
                LocalDate maxReportDate = baseEntityService.getMaxReportDate(baseEntitySaving, reportDateSaving);
                if (maxReportDate == null) {
                    // получение минимальной отчетной даты из будущих периодов
                    // данная опция необходима если идет загрузка задним числом
                    // допустим когда кредит в первый раз был загружен в марте и потом его загружают в феврале
                    LocalDate minReportDate = baseEntityService.getMinReportDate(baseEntitySaving, reportDateSaving);
                    if (minReportDate == null)
                        throw new UnsupportedOperationException(Errors.compose(Errors.E56, baseEntitySaving.getId()));

                    baseEntityLoaded = baseEntityLoadService.loadBaseEntity(baseEntitySaving.getId(), baseEntitySaving.getRespondentId(),
                            baseEntitySaving.getMetaClass(), minReportDate, reportDateSaving);
                }
                else
                    baseEntityLoaded = baseEntityLoadService.loadBaseEntity(baseEntitySaving.getId(), baseEntitySaving.getRespondentId(),
                            baseEntitySaving.getMetaClass(), maxReportDate, reportDateSaving);
            }

            baseEntityApplied = applyBaseEntityAdvanced(respondentId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
        }

        baseEntityApplied.setOperation(baseEntitySaving.getOperation());

        return baseEntityApplied;
    }

    @Override
    public void applyToDb(BaseEntityManager baseEntityManager) {
        if (baseEntityManager.getInsertedEntities().size() > 0) {
            for (BaseEntity baseEntity : baseEntityManager.getInsertedEntities()) {
                baseEntity = eavHubService.insert(baseEntity);

                insertBaseEntityToDb(EavDbSchema.EAV_DATA, baseEntity);

                // необходимо присвоить id сущности которая затем инсертится в схему EAV_XML
                BaseEntity baseEntitySaving = baseEntityManager.getBaseEntityPairs().get(baseEntity.getUuid().toString());
                baseEntitySaving.setId(baseEntity.getId());
            }
        }

        if (baseEntityManager.getNewHistoryEntities().size() > 0) {
            for (BaseEntity baseEntity : baseEntityManager.getNewHistoryEntities())
                insertBaseEntityToDb(EavDbSchema.EAV_DATA, baseEntity);
        }

        if (baseEntityManager.getUpdatedEntities().size() > 0) {
            for (BaseEntity baseEntity : baseEntityManager.getUpdatedEntities())
                updateBaseEntityInDb(baseEntity);
        }

        //TODO: обрабатывать updated, deleted сущности
    }

    private BaseEntity applyBaseEntityBasic(long respondentId, BaseEntity baseEntitySaving, BaseEntityManager baseEntityManager) {
        BaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);
        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        BaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getMetaClass(), baseEntitySaving.getReportDate(), respondentId, baseEntitySaving.getBatchId());
        //TODO: добавить этот костыль
        /*if (baseEntitySaving.getAddInfo() != null)
            baseEntityApplied.setAddInfo(baseEntitySaving.getAddInfo().parentEntity, baseEntitySaving.getAddInfo().isSet,
                    baseEntitySaving.getAddInfo().attributeId);*/

        for (String attribute : baseEntitySaving.getAttributeNames()) {
            BaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);

            // Пропускает закрытые теги на новые сущности <tag/>
            if (baseValueSaving.getValue() == null)
                continue;

            applyBaseValueBasic(respondentId, baseEntityApplied, baseValueSaving, baseEntityManager);
        }

        baseEntityManager.registerAsInserted(baseEntityApplied);

        baseEntityManager.saveBaseEntitySavingAppliedPair(baseEntitySaving, baseEntityApplied);

        return baseEntityApplied;
    }

    private void applyBaseValueBasic(long respondentId, BaseEntity baseEntityApplied, BaseValue baseValueSaving, BaseEntityManager baseEntityManager) {
        MetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E60));

        BaseContainer baseContainer = baseValueSaving.getBaseContainer();
        if (baseContainer != null && !(baseContainer instanceof BaseEntity))
            throw new IllegalStateException(Errors.compose(Errors.E59, metaAttribute.getName()));

        MetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex()) {
            if (metaType.isSet()) {
                MetaSet childMetaSet = (MetaSet) metaType;
                BaseSet childBaseSet = (BaseSet) baseValueSaving.getValue();

                BaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMetaType());
                for (BaseValue childBaseValue : childBaseSet.getValues()) {
                    BaseEntity childBaseEntity = (BaseEntity) childBaseValue.getValue();

                    // все immutable сущности должны иметь id который должен присваивается в методе prepare
                    // если код зашел в данный if то это означает что методы отработали не правильно или данные переданы не корректные
                    if (metaAttribute.isImmutable() && childBaseEntity.getValueCount() != 0 && childBaseEntity.getId() == null)
                        throw new IllegalStateException("Ошибка immutable сущности");

                    BaseEntity childBaseEntityApplied = apply(respondentId, childBaseEntity, null, baseEntityManager);

                    BaseValue childBaseValueApplied = new BaseValue(childBaseEntityApplied);
                    childBaseSetApplied.put(childBaseValueApplied);
                }

                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseSetApplied));
            } else {
                if (metaAttribute.isImmutable()) {
                    BaseEntity childBaseEntity = (BaseEntity) baseValueSaving.getValue();

                    // все immutable сущности должны иметь id который должен присваивается в методе prepare
                    // если код зашел в данный if то это означает что методы отработали не правильно или данные переданы не корректные
                    // сущность должен иметь значения (передаются парсером) для его идентификаций
                    if (childBaseEntity.getValueCount() == 0 || childBaseEntity.getId() == null)
                        throw new IllegalStateException("Ошибка immutable сущности");

                    BaseValue baseValueApplied = new BaseValue(childBaseEntity);
                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                } else {
                    BaseEntity childBaseEntity = (BaseEntity) baseValueSaving.getValue();
                    BaseEntity childBaseEntityApplied = apply(respondentId, childBaseEntity, null, baseEntityManager);

                    baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseEntityApplied));
                }
            }
        } else {
            if (metaType.isSet()) {
                MetaSet childMetaSet = (MetaSet) metaType;
                BaseSet childBaseSet = (BaseSet) baseValueSaving.getValue();

                BaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMetaType());
                for (BaseValue childBaseValue : childBaseSet.getValues())
                    childBaseSetApplied.put(new BaseValue(childBaseValue.getValue()));

                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseSetApplied));
            } else {
                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(baseValueSaving.getValue()));
            }
        }
    }

    private BaseEntity applyBaseEntityAdvanced(long respondentId, BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager) {
        BaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);
        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        MetaClass metaClass = baseEntitySaving.getMetaClass();

        //TODO: детально пока не понятный код почему в prepare не был присвоен id
        // устанавливает id для !metaClass.isSearchable()
        if (baseEntitySaving.getId() == null && baseEntityLoaded.getId() != null)
            baseEntitySaving.setId(baseEntityLoaded.getId());

        // создаем новую сущность чтобы потом все изменения выполнять над ней
        BaseEntity baseEntityApplied = new BaseEntity(baseEntityLoaded.getMetaClass());
        baseEntityApplied.setId(baseEntitySaving.getId());
        baseEntityApplied.setReportDate(baseEntitySaving.getReportDate());
        baseEntityApplied.setRespondentId(baseEntitySaving.getRespondentId());
        baseEntityApplied.setOperation(baseEntitySaving.getOperation());
        baseEntityApplied.setUserId(baseEntitySaving.getUserId());
        baseEntityApplied.setBatchId(baseEntitySaving.getBatchId());

        //TODO: детально пока не понятный код
        // почему заново подгружать сущность если baseEntityLoading уже подгружали
        if (baseEntityService.existsBaseEntity(baseEntitySaving, baseEntitySaving.getReportDate())) {
            baseEntityLoaded = baseEntityLoadService.loadBaseEntity(baseEntitySaving.getId(), baseEntitySaving.getRespondentId(),
                    baseEntitySaving.getMetaClass(), baseEntitySaving.getReportDate(), baseEntitySaving.getReportDate());
            baseEntityLoaded.setUserId(baseEntitySaving.getUserId());
            baseEntityLoaded.setBatchId(baseEntitySaving.getBatchId());
        }

        for (String attrName : metaClass.getAttributeNames()) {
            BaseValue baseValueSaving = baseEntitySaving.getBaseValue(attrName);
            BaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attrName);

            final MetaAttribute metaAttribute = metaClass.getMetaAttribute(attrName);
            final MetaType metaType = metaAttribute.getMetaType();

            //TODO: пересмотреть
            if (baseValueSaving == null && baseValueLoaded != null && !metaAttribute.isNullable())
                baseEntityApplied.put(attrName, baseValueLoaded);

            //TODO: пересмотреть
            if (baseValueSaving == null && metaAttribute.isNullable()) {
                baseValueSaving = new BaseValue();
                baseValueSaving.setBaseContainer(baseEntitySaving);
                baseValueSaving.setMetaAttribute(metaAttribute);
            }

            if (baseValueSaving == null)
                continue;

            if (metaType.isComplex()) {
                if (metaType.isSet()) {
                    BaseSet childBaseSetApplied = applyComplexSet(respondentId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                    baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseSetApplied));
                }
                else
                    applyComplexValue(respondentId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            } else {
                if (metaType.isSet())
                    applySimpleSet(respondentId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else {
                    applySimpleValue(respondentId, metaAttribute, baseEntityApplied, baseEntityLoaded, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
            }
        }

        LocalDate reportDateSaving = baseEntitySaving.getReportDate();
        LocalDate reportDateLoaded = baseEntityLoaded.getReportDate();

        // проверяем сущность, если новая то инсертим, если же была ранее загружена то проверяем отчетные даты
        // если уже запись существует за отчетный период то делает просто update
        // иначе по сущности добавляем новую историю
        if (baseEntityApplied.getId() == null)
            baseEntityManager.registerAsInserted(baseEntityApplied);
        else {
            if (reportDateSaving.compareTo(reportDateLoaded) == 0) {
                int changeds = 0;

                for (MetaAttribute attribute : metaClass.getAttributes()) {
                    BaseValue childBaseValueLoaded = baseEntityLoaded.getBaseValue(attribute.getName());
                    BaseValue childBaseValueApplied = baseEntityApplied.getBaseValue(attribute.getName());

                    if (childBaseValueApplied == null && childBaseValueApplied == null)
                        continue;

                    if (childBaseValueApplied != null) {
                        childBaseValueApplied.setChanged(Boolean.FALSE);
                        if (!childBaseValueApplied.equalsByValue(childBaseValueLoaded)) {
                            childBaseValueApplied.setChanged(Boolean.TRUE);
                            changeds++;
                        }
                    }
                }

                // если поменял хотя бы один атрибут или был сдвиг отчетной даты
                if (changeds > 0)
                    baseEntityManager.registerAsUpdated(baseEntityApplied);
            }
            else
                baseEntityManager.registerAsNewHistory(baseEntityApplied);
        }

        //TODO: добавить этот костыль
        /*if (baseEntitySaving.getAddInfo() != null)
            baseEntityApplied.setAddInfo(baseEntitySaving.getAddInfo().parentEntity, baseEntitySaving.getAddInfo().isSet,
                    baseEntitySaving.getAddInfo().attributeId);*/

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    //TODO: не полностью реализован
    //TODO: черновой вариант
    private void applySimpleValue(long respondentId, MetaAttribute metaAttribute, BaseEntity baseEntityApplied, BaseEntity baseEntityLoaded, BaseValue baseValueSaving,
                                 BaseValue baseValueLoaded, BaseEntityManager baseEntityManager) {
        MetaClass metaClass = baseEntityApplied.getMetaClass();

        MetaType metaType = metaAttribute.getMetaType();
        MetaValue metaValue = (MetaValue) metaType;

        // TODO: изменение ключевых полей

        LocalDate reportDateLoaded = baseEntityLoaded.getReportDate();
        LocalDate reportDateSaving = baseEntityApplied.getReportDate();

        int compareDates = reportDateSaving.compareTo(reportDateLoaded);

        if (baseValueLoaded != null) {
            if (baseValueSaving == null) {
                // в случае если атрибут не пришел в теге но за другой период у нас есть значение
                // обогащаем сущность другим значением
                if (!metaAttribute.isFinal() && compareDates == 1)
                    baseEntityApplied.put(metaAttribute.getName(), new BaseValue(baseValueLoaded.getValue()));
            }
            else {
                // если ранее было значение и теперь пришло новое значение то просто заменяем новым
                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(baseValueSaving.getValue()));
            }
        }
        else {
            if (baseValueSaving != null)
                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(baseValueSaving.getValue()));
        }
    }

    private void applyComplexValue(long respondentId, BaseEntity baseEntity, BaseValue baseValueSaving,
                                   BaseValue baseValueLoaded, BaseEntityManager baseEntityManager) {
        //TODO: необходимо реализовать метод
    }

    private void applySimpleSet(long respondentId, BaseEntity baseEntity, BaseValue baseValueSaving,
                               BaseValue baseValueLoaded, BaseEntityManager baseEntityManager) {
        //TODO: необходимо реализовать метод
    }

    private BaseSet applyComplexSet(long respondentId, BaseEntity baseEntity, BaseValue baseValueSaving,
                                    BaseValue baseValueLoaded, BaseEntityManager baseEntityManager) {
        MetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        MetaType metaType = metaAttribute.getMetaType();

        MetaSet childMetaSet = (MetaSet) metaType;
        MetaType childMetaType = childMetaSet.getMetaType();
        MetaClass childMetaClass = (MetaClass) childMetaType;

        BaseSet childBaseSetSaving = (BaseSet) baseValueSaving.getValue();
        BaseSet childBaseSetLoaded = null;
        if (baseValueLoaded != null && baseValueLoaded.getValue() != null)
            childBaseSetLoaded = (BaseSet) baseValueLoaded.getValue();

        BaseSet childBaseSetApplied = new BaseSet(childMetaClass);

        Set<UUID> processedUuIds = new HashSet<>();

        for (BaseValue childBaseValueSaving : childBaseSetSaving.getValues()) {
            BaseEntity childBaseEntitySaving = (BaseEntity) childBaseValueSaving.getValue();
            BaseEntity childBaseEntityApplied = null;

            if (childBaseSetLoaded != null && childBaseEntitySaving.getId() != null) {
                for (BaseValue childBaseValueLoaded : childBaseSetLoaded.getValues()) {
                    BaseEntity childBaseEntityLoaded =  (BaseEntity) childBaseValueLoaded.getValue();

                    if (childBaseEntityLoaded.equals(childBaseEntitySaving) || childBaseEntityLoaded.getId().equals(childBaseEntitySaving.getId())) {
                        processedUuIds.add(childBaseEntityLoaded.getUuid());

                        // ранее загруженная сущность, отправляем ее на обработку
                        childBaseEntityApplied = applyBaseEntityAdvanced(respondentId, childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager);
                        childBaseSetApplied.put(new BaseValue(childBaseEntityApplied));
                        break;
                    }
                }
            }

            // новая сущность или сущность которая ранее была подгружена но потом перестала быть в массиве,
            // отправляем сущность на обработку
            if (childBaseEntityApplied == null && childBaseEntitySaving.getId() == null) {
                childBaseEntityApplied = apply(respondentId, childBaseEntitySaving, null, baseEntityManager);
                childBaseSetApplied.put(new BaseValue(childBaseEntityApplied));
            }
            else if (childBaseEntityApplied == null && childBaseEntitySaving.getId() != null) {
                // TODO: возможно необходимо вызывать applyBaseEntityAdvanced
                childBaseEntityApplied = apply(respondentId, childBaseEntitySaving, null, baseEntityManager);
                childBaseSetApplied.put(new BaseValue(childBaseEntityApplied));
            }
        }

        if (childBaseSetLoaded != null) {
            for (BaseValue childBaseValueLoaded : childBaseSetLoaded.getValues()) {
                BaseEntity childBaseEntityLoaded =  (BaseEntity) childBaseValueLoaded.getValue();
                if (processedUuIds.contains(childBaseEntityLoaded.getUuid()))
                    continue;

                // кумулятивный сет если даже сущность не пришла в батче то все равно дополняем сет из ранее загруженных
                if (metaAttribute.isCumulative())
                    childBaseSetApplied.put(new BaseValue(childBaseEntityLoaded));
                // если не кумулятивный сет и если значение ранее было подгружено но в этот раз не пришла
                // в батче то удаляем сущность если на нее нет ссылок
                else {
                    // TODO: удалить сущности если
                }
            }
        }

        return childBaseSetApplied;
    }

    @Override
    public void applyToSchemaEavXml(final BaseEntity baseEntity) {
        logger.info("applyToSchemaEavXml: " + baseEntity);

        //TODO: необходимо создавать baseEntityApplied
        //TODO: необходимо добавить комментарий по алгоритму

        // TODO: старый коммент Пропускает закрытые теги на новые сущности <tag/>
        for (String attribute: baseEntity.getAttributeNames()) {
            MetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
            MetaType metaType = metaAttribute.getMetaType();

            BaseValue baseValue = baseEntity.getBaseValue(attribute);
            if (baseValue != null && baseValue.getValue() != null && metaType.isComplex() && !metaAttribute.isImmutable())
                applyBaseValueToSchemaEavXml(metaAttribute, baseValue);
        }

        insertBaseEntityToDb(EavDbSchema.EAV_XML, baseEntity);
    }

    private void applyBaseValueToSchemaEavXml(MetaAttribute metaAttribute, final BaseValue baseValue) {
        if (metaAttribute == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E60));

        BaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer != null && !(baseContainer instanceof BaseEntity))
            throw new IllegalArgumentException(Errors.compose(Errors.E59, metaAttribute.getName()));

        MetaType metaType = metaAttribute.getMetaType();

        if (!metaType.isComplex())
            throw new IllegalArgumentException("Данный метод обрабатывает только комплексные атрибуты");

        if (metaType.isSet()) {
            BaseSet childBaseSet = (BaseSet) baseValue.getValue();

            for (BaseValue childBaseValue : childBaseSet.getValues())
                applyToSchemaEavXml((BaseEntity) childBaseValue.getValue());
        }
        else
            applyToSchemaEavXml((BaseEntity) baseValue.getValue());
    }

    /**
     * метод делает insert сущности в базу => одна сущность = одна запись в базе
     * наименование таблицы и столбцов берет из мета данных
     * в любой таблице есть обязательные поля (см. код) помимо атрибутов мета класса
     * */
    private void insertBaseEntityToDb(EavDbSchema schema, final BaseEntity insertedBaseEntity) {
        if (insertedBaseEntity.getId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id ");

        MetaClass metaClass = insertedBaseEntity.getMetaClass();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(npJdbcTemplate.getJdbcTemplate());
        simpleJdbcInsert.setSchemaName(schema.name());
        simpleJdbcInsert.setTableName(metaClass.getTableName());

        // обязательные поля в реляционной таблице: ENTITY_ID, CREDITOR_ID, REPORT_DATE, BATCH_ID
        Set<String> columns = new HashSet<>(Arrays.asList("ENTITY_ID", "CREDITOR_ID", "REPORT_DATE", "BATCH_ID"));
        columns.addAll(insertedBaseEntity.getAttributeNames().stream()
                .map(attributeName -> insertedBaseEntity.getMetaAttribute(attributeName).getColumnName())
                .collect(Collectors.toList()));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("BATCH_ID", insertedBaseEntity.getBatchId());
        params.addValue("ENTITY_ID", insertedBaseEntity.getId());
        params.addValue("CREDITOR_ID", insertedBaseEntity.getRespondentId());
        params.addValue("REPORT_DATE", Converter.convertToSqlDate(insertedBaseEntity.getReportDate()));

        for (String attribute: insertedBaseEntity.getAttributeNames()) {
            MetaAttribute metaAttribute = insertedBaseEntity.getMetaAttribute(attribute);
            BaseValue baseValue = insertedBaseEntity.getBaseValue(attribute);

            Object sqlValue = convertBaseValueToRmValue(schema.name(), metaAttribute, baseValue);
            params.addValue(metaAttribute.getColumnName(), sqlValue);
        }

        simpleJdbcInsert.setColumnNames(new ArrayList<>(columns));

        int count = simpleJdbcInsert.execute(params);
        if (count == 0)
            throw new IllegalArgumentException("Ошибка завершения DML операций по таблице " + String.join(".", metaClass.getSchemaXml(), metaClass.getTableName()));
    }

    /**
     * метод делает update сущности в БД (принцип => одна сущность = одна запись в базе)
     * необходимо передавать сущность лишь в случае если есть атрибуты которые
     * изменились, иначе метод выбрасывает исключение.
     * update производится лишь по измененным атрибутам(столбцам)
     * поиск записи сущности делается по полям CREDITOR_ID, ENTITY_ID, REPORT_DATE (PRIMARY KEY)
     * столбец BATCH_ID добавляется в update запрос чтобы зафиксировать последний батч по которому прилетела инфа
     * пример SQL запроса:
     * update EAV_DATA.PLEDGE
     *   set BATCH_ID = 45,
     *       VALUE = 30000
     *  where CREDITOR_ID = :CREDITOR_ID
     *    and ENTITY_ID = :ENTITY_ID
     *    and REPORT_DATE = :REPORT_DATE
     * */
    private void updateBaseEntityInDb(final BaseEntity updatedBaseEntity) {
        if (updatedBaseEntity.getId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id");
        if (updatedBaseEntity.getValueCount() == 0)
            throw new IllegalArgumentException("У сущности отсутствуют значения");

        long changedAttributes = updatedBaseEntity.getValues().stream().filter(BaseValue::isChanged).count();
        if (changedAttributes == 0)
            throw new IllegalArgumentException("Нет измененных атрибутов у сущности");

        MetaClass metaClass = updatedBaseEntity.getMetaClass();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("BATCH_ID", updatedBaseEntity.getBatchId());
        params.addValue("ENTITY_ID", updatedBaseEntity.getId());
        params.addValue("CREDITOR_ID", updatedBaseEntity.getRespondentId());
        params.addValue("REPORT_DATE", Converter.convertToSqlDate(updatedBaseEntity.getReportDate()));

        StringBuilder columns = new StringBuilder();
        columns.append("BATCH_ID = :BATCH_ID");

        for (String attrName: updatedBaseEntity.getAttributeNames()) {
            MetaAttribute metaAttribute = updatedBaseEntity.getMetaAttribute(attrName);
            String columnName = metaAttribute.getColumnName();

            BaseValue baseValue = updatedBaseEntity.getBaseValue(attrName);
            if (baseValue == null || !baseValue.isChanged())
                continue;

            Object value = null;
            if (baseValue.getValue() != null)
                value = convertBaseValueToRmValue(metaClass.getSchemaData(), metaAttribute, baseValue);

            params.addValue(columnName, value);

            columns.append(",\n");
            columns.append(columnName).append(" = :").append(columnName);
        }

        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ").append(metaClass.getSchemaData()).append(".").append(metaClass.getTableName());
        updateQuery.append("set ");
        updateQuery.append(columns).append("\n");
        updateQuery.append("where CREDITOR_ID = :CREDITOR_ID\n");
        updateQuery.append("  and ENTITY_ID = :ENTITY_ID\n");
        updateQuery.append("  and REPORT_DATE = :REPORT_DATE\n");

        int count = npJdbcTemplate.update(updateQuery.toString(), params);
        if (count == 0)
            throw new IllegalArgumentException("Ошибка завершения DML операций по таблице " + String.join(".", metaClass.getSchemaXml(), metaClass.getTableName()));
    }

    /**
     * метод конвертирует EAV значение в значение реляционной таблицы
     * для комплексных сетов берем только id сущностей, конвертируем коллекцию в обычный массив
     * для обычных сетов берем массив значений
     * для скалярных сущностей берем только id самой сущности
     * для скалярных примитивных значений берем само значение привиденное к jdbc
     * */
    private Object convertBaseValueToRmValue(String schema, MetaAttribute metaAttribute, BaseValue baseValue) {
        try {
            MetaType metaType = metaAttribute.getMetaType();

            Object value;
            if (metaType.isSet()) {
                Object array;
                BaseSet baseSet = (BaseSet) baseValue.getValue();
                if (metaType.isComplex())
                    array = new ArrayList<>(baseSet.getValues().stream()
                            .map(ed -> ((BaseEntity) ed.getValue()).getId())
                            .collect(Collectors.toSet())).toArray();
                else
                    array = new ArrayList<>(baseSet.getValues().stream()
                            .map(ed -> ((BaseValue) ed.getValue()).getValue())
                            .collect(Collectors.toSet())).toArray();

                // особенность Oracle, для создания массива обязательно пользоваться createARRAY а не createArrayOf
                // также необходимо получить соединение с базой spring утилитой иначе получим только прокси обьект
                Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                OracleConnection oraConn = conn.unwrap(OracleConnection.class);

                value = oraConn.createARRAY(String.join(".", schema, metaAttribute.getColumnType()), array);
            } else if (metaType.isComplex())
                value = ((BaseEntity) baseValue.getValue()).getId();
            else
                value = MetaDataType.convertToRmValue(((MetaValue) metaAttribute.getMetaType()).getMetaDataType(), baseValue.getValue());

            return value;
        } catch (SQLException e) {
            throw new UsciException(e.getMessage());
        }
    }

}
