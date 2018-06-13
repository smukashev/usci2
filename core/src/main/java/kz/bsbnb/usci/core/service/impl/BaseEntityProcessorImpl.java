package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.model.EavDbSchema;
import kz.bsbnb.usci.core.service.BaseEntityManager;
import kz.bsbnb.usci.core.service.BaseEntityProcessor;
import kz.bsbnb.usci.core.service.EavHubService;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.UsciException;
import kz.bsbnb.usci.model.eav.base.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BaseEntityProcessorImpl implements BaseEntityProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorImpl.class);

    private final EavHubService eavHubService;
    private final NamedParameterJdbcTemplate npJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public BaseEntityProcessorImpl(EavHubService eavHubService,
                                   NamedParameterJdbcTemplate npJdbcTemplate,
                                   JdbcTemplate jdbcTemplate) {
        this.eavHubService = eavHubService;
        this.npJdbcTemplate = npJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public BaseEntity processBaseEntity(final BaseEntity baseEntitySaving, LocalDate reportDate) throws SQLException {
        BaseEntityManager baseEntityManager = new BaseEntityManager();

        BaseEntity baseEntityPrepared;
        BaseEntity baseEntityApplied;

        // все сущности кроме справочников должны иметь респондента
        if (!baseEntitySaving.getMetaClass().isDictionary() && baseEntitySaving.getRespondentId() == 0)
            throw new IllegalStateException(Errors.compose(Errors.E197));

        baseEntityManager.registerRespondentId(baseEntitySaving.getRespondentId());

        baseEntityPrepared = prepareBaseEntity(baseEntitySaving.clone());

        // проверка сущности на бизнес правила
        // временно отключен так как еще не реализован
        //checkForRules(baseEntityPrepared);

        switch (baseEntityPrepared.getOperation()) {
            case INSERT:
                if (baseEntityPrepared.getId() > 0)
                    throw new UsciException(Errors.compose(Errors.E196, baseEntityPrepared.getId()));

                baseEntityApplied = applyBaseEntity(baseEntitySaving.getRespondentId(), baseEntityPrepared, null, baseEntityManager);

                // временно отключен так как еще не реализован
                /*if (rulesEnabledForUser(baseEntitySaving))
                    processLogicControl(baseEntityApplied);*/

                applyToDb(baseEntityManager);

                // заливаем данные непосредственно в схему EAV_XML
                applyBaseEntityToSchemaEavXml(baseEntityPrepared);

                break;
            case UPDATE:
                //TODO:
                throw new UnsupportedOperationException("Not yet implemented");
            case DELETE:
                //TODO:
                throw new UnsupportedOperationException("Not yet implemented");
            case CLOSE:
                //TODO:
                throw new UnsupportedOperationException("Not yet implemented");
            case OPEN:
                //TODO:
                throw new UnsupportedOperationException("Not yet implemented");
            case CHECKED_REMOVE:
                //TODO:
                throw new UnsupportedOperationException("Not yet implemented");
            default:
                throw new UnsupportedOperationException(Errors.compose(Errors.E118, baseEntityPrepared.getOperation()));
        }

        return baseEntityApplied;
    }

    private void applyToDb(BaseEntityManager baseEntityManager) {
        try {
            if (baseEntityManager.getInsertedEntities().size() > 0) {
                for (BaseEntity baseEntity : baseEntityManager.getInsertedEntities()) {
                    baseEntity = eavHubService.insert(baseEntity);

                    insertBaseEntityToDb(EavDbSchema.EAV_DATA, baseEntity);

                    // необходимо присвоить id сущности которая затем инсертится в схему EAV_XML
                    BaseEntity baseEntitySaving = baseEntityManager.getBaseEntityPairs().get(baseEntity.getUuid().toString());
                    baseEntitySaving.setId(baseEntity.getId());
                }
            }
        } catch (SQLException e) {
            //TODO: add logger or throw exception
            e.printStackTrace();
        }
    }

    private BaseEntity applyBaseEntity(long respondentId, final BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager) {
        logger.info("applyBaseEntity: " + baseEntitySaving);

        BaseEntity baseEntityApplied;

        // новые сущности или сущности не имеющие ключевые атрибуты
        if (baseEntitySaving.getId() < 1 || !baseEntitySaving.getMetaClass().isSearchable())
            baseEntityApplied = applyBaseEntityBasic(respondentId, baseEntitySaving, baseEntityManager);
        else {
            if (baseEntityLoaded == null) {
                //TODO: implement
            }

            baseEntityApplied = applyBaseEntityAdvanced(respondentId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
        }

        baseEntityApplied.setOperation(baseEntitySaving.getOperation());

        return baseEntityApplied;
    }

    public BaseEntity applyBaseEntityBasic(long respondentId, BaseEntity baseEntitySaving, BaseEntityManager baseEntityManager) {
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

    public void applyBaseValueBasic(long respondentId, BaseEntity baseEntityApplied, BaseValue baseValueSaving, BaseEntityManager baseEntityManager) {
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
                    // код отключен пока Б.Ахметов не загрузит справочники
                    if (metaAttribute.isImmutable() && childBaseEntity.getValueCount() != 0 && childBaseEntity.getId() < 1)
                        throw new IllegalStateException("Ошибка immutable сущности");

                    //TODO: присвоение id добавлен в целях тестирования, потом удалить
                    /*if (metaAttribute.isImmutable())
                        childBaseEntity.setId(Math.round(Math.random()) + 1);*/

                    BaseEntity childBaseEntityApplied = applyBaseEntity(respondentId, childBaseEntity, null, baseEntityManager);

                    BaseValue childBaseValueApplied = new BaseValue(childBaseEntityApplied);
                    childBaseSetApplied.put(childBaseValueApplied);
                }

                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseSetApplied));
            } else {
                if (metaAttribute.isImmutable()) {
                    BaseEntity childBaseEntity = (BaseEntity) baseValueSaving.getValue();

                    // все immutable сущности должны иметь id который должен присваивается в методе prepare
                    // если код зашел в данный if то это означает что методы отработали не правильно или данные переданы не корректные
                    // сущности должен иметь значения (передаются парсером) для его идентификаций
                    // код отключен пока Б.Ахметов не загрузит справочники
                    if (childBaseEntity.getValueCount() == 0 || childBaseEntity.getId() < 1)
                        throw new IllegalStateException("Ошибка immutable сущности");

                    //TODO: присвоение id добавлен в целях тестирования, потом удалить
                    //childBaseEntity.setId(Math.round(Math.random()) + 1);

                    BaseValue baseValueApplied = new BaseValue(childBaseEntity);
                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                } else {
                    BaseEntity childBaseEntity = (BaseEntity) baseValueSaving.getValue();
                    BaseEntity childBaseEntityApplied = applyBaseEntity(respondentId, childBaseEntity, null, baseEntityManager);

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

    public BaseEntity applyBaseEntityAdvanced(long respondentId, BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager) {
        //TODO:
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void checkForRules(final BaseEntity baseEntity) {
        //TODO:
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private boolean rulesEnabledForUser(final BaseEntity baseEntity) {
        // бизнес правило не предусмотрено для сотрудника НБРК Ж.Куатова
        return baseEntity.getUserId() == null || baseEntity.getUserId() != 100500L;
    }

    private void processLogicControl(final BaseEntity baseEntity) {
        //TODO: добавить поддержку бизнес правил
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void applyBaseEntityToSchemaEavXml(final BaseEntity baseEntity) throws SQLException {
        logger.info("applyBaseEntityToSchemaEavXml: " + baseEntity);

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

    private void applyBaseValueToSchemaEavXml(MetaAttribute metaAttribute, final BaseValue baseValue) throws SQLException {
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
                applyBaseEntityToSchemaEavXml((BaseEntity) childBaseValue.getValue());
        }
        else
            applyBaseEntityToSchemaEavXml((BaseEntity) baseValue.getValue());
    }

    // метод присваивает id всем найденным сущностям,
    // заполняет родительские узлы у дочерних узлов
    // TODO: черновой вариант
    public BaseEntity prepareBaseEntity(final BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMetaClass();

        // получаю все ключевые комплексные атрибуты сущности, сеты
        // чтобы через поиск присвоить им id
        baseEntity.getAttributes()
            .filter(attribute -> {
                MetaType metaType = attribute.getMetaType();
                BaseValue baseValue = baseEntity.getBaseValue(attribute.getName());
                return attribute.isKey() && metaType.isComplex() && baseValue!= null;
            })
            .forEach(attribute -> {
                MetaType metaType = attribute.getMetaType();
                BaseValue baseValue = baseEntity.getBaseValue(attribute.getName());

                if (metaType.isSet()) {
                    BaseSet childBaseSet = (BaseSet) baseValue.getValue();

                    for (BaseValue childBaseValue : childBaseSet.getValues()) {
                        BaseEntity childBaseEntity = (BaseEntity) childBaseValue.getValue();

                        if (childBaseEntity.getValueCount() != 0)
                            prepareBaseEntity((BaseEntity) childBaseValue.getValue());
                    }
                } else {
                    BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();

                    if (childBaseEntity.getValueCount() != 0)
                        prepareBaseEntity(childBaseEntity);
                }
            });

        if (metaClass.isSearchable() && baseEntity.getId() == 0) {
            Long baseEntityId = search(baseEntity);
            if (baseEntityId > 0)
                baseEntity.setId(baseEntityId);
        }

        if (metaClass.parentIsKey() && baseEntity.getId() == 0)
            baseEntity.setId(search(baseEntity));

        //TODO: добавить заполнение родительских узлов у сущностей
        //TODO:

        return baseEntity;
    }

    private long search(BaseEntity baseEntity) {
        Long baseEntityId = eavHubService.find(baseEntity);
        return baseEntityId == null ? 0 : baseEntityId;
    }

    // инсерт сущности в базу => одна сущность = одна запись в базе
    // наименование таблицы и столбцов берем из мета данных
    // также не забываем что в любой таблице есть обязательные поля (см. код) помимо атрибутов мета класса
    private void insertBaseEntityToDb(EavDbSchema schema, final BaseEntity baseEntity) throws SQLException {
        if (baseEntity.getId() < 1)
            throw new IllegalArgumentException("У сущности отсутствует id ");

        MetaClass metaClass = baseEntity.getMetaClass();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(npJdbcTemplate.getJdbcTemplate());
        simpleJdbcInsert.setSchemaName(schema.name());
        simpleJdbcInsert.setTableName(metaClass.getTableName());

        // обязательные поля в реляционной таблице: entityId, creditorId, reportDate, batchId
        Set<String> columns = new HashSet<>(Arrays.asList("ENTITY_ID", "CREDITOR_ID", "REPORT_DATE", "BATCH_ID"));
        columns.addAll(baseEntity.getAttributeNames().stream()
                .map(attributeName -> baseEntity.getMetaAttribute(attributeName).getColumnName())
                .collect(Collectors.toList()));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("BATCH_ID", baseEntity.getBatchId());
        params.addValue("ENTITY_ID", baseEntity.getId());
        params.addValue("CREDITOR_ID", baseEntity.getRespondentId());
        params.addValue("REPORT_DATE", Converter.convertToSqlDate(baseEntity.getReportDate()));

        for (String attribute: baseEntity.getAttributeNames()) {
            MetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
            BaseValue baseValue = baseEntity.getBaseValue(attribute);

            Object sqlValue = convertBaseValueToRmValue(schema, metaAttribute, baseValue);
            params.addValue(metaAttribute.getColumnName(), sqlValue);
        }

        simpleJdbcInsert.setColumnNames(new ArrayList<>(columns));

        int count = simpleJdbcInsert.execute(params);
        if (count == 0)
            throw new IllegalArgumentException("Ошибка завершения DML операций по таблице " + String.join(".", metaClass.getSchemaXml(), metaClass.getTableName()));
    }

    // конвертирует EAV значение в значение реляционной таблицы
    // для комплексных сетов берем только id сущностей, конвертируем коллекцию конвертируем в обычный массив
    // для обычных сетов берем массив значений
    // для скалярных сущностей берем только id самой сущности
    // для скалярных примитивных значений берем само значение
    private Object convertBaseValueToRmValue(EavDbSchema schema, MetaAttribute metaAttribute, BaseValue baseValue) throws SQLException {
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
                        .map(ed -> ((BaseValue)ed.getValue()).getValue())
                        .collect(Collectors.toSet())).toArray();

            // особенность Oracle, для создания массива обязательно пользоваться createARRAY а не createArrayOf
            // также необходимо получить соединение с базой spring утилитой иначе получим только прокси обьект
            Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            OracleConnection oraConn = conn.unwrap(OracleConnection.class);

            value = oraConn.createARRAY(String.join(".", schema.name(), metaAttribute.getColumnType()), array);
        }
        else if (metaType.isComplex())
            value = ((BaseEntity) baseValue.getValue()).getId();
        else
            value = MetaDataType.convertToRmValue(((MetaValue) metaAttribute.getMetaType()).getMetaDataType(), baseValue.getValue());

        return value;
    }

}
