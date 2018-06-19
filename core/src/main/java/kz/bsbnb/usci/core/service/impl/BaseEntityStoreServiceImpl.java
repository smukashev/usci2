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
public class BaseEntityStoreServiceImpl implements BaseEntityStoreService {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorImpl.class);

    private final EavHubService eavHubService;
    private final BaseEntityService baseEntityService;
    private final BaseEntityLoadService baseEntityLoadService;
    private final NamedParameterJdbcTemplate npJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public BaseEntityStoreServiceImpl(EavHubService eavHubService,
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

    /**
     * метод диспетчер отвечает за обработку сущности и выполняет:
     * новые сущности непосредственно отправляет на обработку
     * если сущность существует то предварительно подгружает ее из БД и потом отправляет на обработку
     * предварительно загружать сущность из БД нужно чтобы делать сверку с данными которые прислали
     * алгоритм обработки существующей сущности см. в методе processExistingBaseEntity
     * */
    @Override
    public BaseEntity processBaseEntity(final BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, final BaseEntityManager baseEntityManager) {
        BaseEntity baseEntityApplied;

        if (baseEntitySaving.getId() == null)
            baseEntityApplied = processNewBaseEntity(baseEntitySaving, baseEntityManager);
        else {
            if (baseEntityLoaded == null) {
                LocalDate reportDateSaving = baseEntitySaving.getReportDate();

                // получение максимальной отчетной даты из прошедших периодов
                LocalDate maxReportDate = baseEntityService.getMaxReportDate(baseEntitySaving, reportDateSaving);
                if (maxReportDate == null) {
                    // получение минимальной отчетной даты из будущих периодов
                    // данная опция необходима если идет загрузка задней датой
                    // допустим когда кредит в первый* раз был загружен в марте и потом его загружают в феврале
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

            baseEntityApplied = processExistingBaseEntity(null, baseEntitySaving, baseEntityLoaded, baseEntityManager);
        }

        return baseEntityApplied;
    }

    /**
     * метод выполняет DML операций в БД
     * - создает запись по новой сущности
     * - изменяет историю по существующим сущностям
     * - добавляет историю по существующим сущностям
     * - сдвигает отчетную дату назад (изменение отчетной даты когда прислали сущность задней датой и по ней нет никаких изменений)
     * - удаляет сущность
     * */
    @Override
    public void processBaseManager(BaseEntityManager baseEntityManager) {
        for (BaseEntity baseEntity : baseEntityManager.getInsertedEntities()) {
            baseEntity = eavHubService.insert(baseEntity);

            insertBaseEntityToDb(EavDbSchema.EAV_DATA, baseEntity);

            // необходимо присвоить id сущности которая затем инсертится в схему EAV_XML
            BaseEntity baseEntitySaving = baseEntityManager.getBaseEntityPairs().get(baseEntity.getUuid().toString());
            baseEntitySaving.setId(baseEntity.getId());
        }

        for (BaseEntity baseEntity : baseEntityManager.getNewHistoryEntities())
            insertBaseEntityToDb(EavDbSchema.EAV_DATA, baseEntity);

        for (BaseEntity baseEntity : baseEntityManager.getUpdatedEntities())
            updateBaseEntityInDb(baseEntity, false);

        for (BaseEntity baseEntity : baseEntityManager.getShiftEntities())
            updateBaseEntityInDb(baseEntity, true);
    }


    /**
     * метод занимается обработкой новых сущностей (то есть тех которыех нет в БД):
     * - создаем новую сущность из реквизитов сущности из парсера.
     * - комплексные сеты: создаем новый сет для манипуляций с ним
     *   каждую сущность сета отправляем на обработку, затем добавляем его в новый сет
     *  - примитивный сет: создаем новый сет из реквизитов сета из парсера, затем мигрируем значения из сета от парсера в новый сет
     * - комплексный скаляр: отправляем атрибут на обработку, затем ложим его в новую сущность
     * - примитивный скаляр: просто ложим значение в новую сущность
     * */
    private BaseEntity processNewBaseEntity(BaseEntity baseEntitySaving, BaseEntityManager baseEntityManager) {
        BaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);
        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        if (baseEntitySaving.getId() != null)
            throw new IllegalArgumentException("Метод обрабатывает только новые сущности");

        BaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getMetaClass(), baseEntitySaving.getReportDate(), baseEntitySaving.getRespondentId(), baseEntitySaving.getBatchId());
        baseEntityApplied.setParent(baseEntitySaving.getParent().getEntity(), baseEntitySaving.getParent().getAttribute());

        for (String attrName : baseEntitySaving.getAttributeNames()) {
            BaseValue baseValueSaving = baseEntitySaving.getBaseValue(attrName);

            // пропускает закрытые теги на новые сущности <tag/>
            if (baseValueSaving.getValue() == null)
                continue;

            MetaAttribute metaAttribute = baseEntitySaving.getMetaAttribute(attrName);
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

                        BaseEntity childBaseEntityApplied = processBaseEntity(childBaseEntity, null, baseEntityManager);
                        childBaseSetApplied.put(new BaseValue(childBaseEntityApplied));
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

                        baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseEntity));
                    } else {
                        BaseEntity childBaseEntity = (BaseEntity) baseValueSaving.getValue();
                        BaseEntity childBaseEntityApplied = processBaseEntity(childBaseEntity, null, baseEntityManager);

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

        baseEntityManager.registerAsInserted(baseEntityApplied);

        baseEntityManager.saveBaseEntitySavingAppliedPair(baseEntitySaving, baseEntityApplied);

        return baseEntityApplied;
    }

    //TODO: добавить обработку изменения ключевых атрибутов
    private BaseEntity processExistingBaseEntity(MetaAttribute baseAttribute, BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager) {
        BaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);
        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        LocalDate reportDateSaving = baseEntitySaving.getReportDate();
        LocalDate reportDateLoaded = baseEntityLoaded.getReportDate();

        int compareDates = reportDateSaving.compareTo(reportDateLoaded);

        MetaClass metaClass = baseEntitySaving.getMetaClass();

        //if (baseEntitySaving.getId() == null)
        //    throw new IllegalArgumentException("Метод обрабатывает сущности которые ранее были подгужены");

        if (baseAttribute != null && baseAttribute.isFinal() && compareDates != 0)
            throw new IllegalArgumentException("Final атрибуты должны действовать только на отчетную дату");

        // устанавливает id для searchable=false
        if (baseEntitySaving.getId() == null && baseEntityLoaded.getId() != null && !metaClass.isSearchable())
            baseEntitySaving.setId(baseEntityLoaded.getId());

        // создаем новый обьект чтобы потом все изменения выполнять над ним
        BaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getId(), baseEntityLoaded.getMetaClass(),
                baseEntitySaving.getReportDate(), baseEntitySaving.getRespondentId(), baseEntitySaving.getBatchId());
        baseEntityApplied.setOperation(baseEntitySaving.getOperation());

        if (baseEntitySaving.getParent() != null)
            baseEntityApplied.setParent(baseEntitySaving.getParent().getEntity(), baseEntitySaving.getParent().getAttribute());

        if (baseEntityService.existsBaseEntity(baseEntitySaving, baseEntitySaving.getReportDate())) {
            baseEntityLoaded = baseEntityLoadService.loadBaseEntity(baseEntitySaving.getId(), baseEntitySaving.getRespondentId(),
                    baseEntitySaving.getMetaClass(), baseEntitySaving.getReportDate(), baseEntitySaving.getReportDate());
            baseEntityLoaded.setUserId(baseEntitySaving.getUserId());
            baseEntityLoaded.setBatchId(baseEntitySaving.getBatchId());
        }

        for (String attrName : metaClass.getAttributeNames()) {
            BaseValue baseValueSaving = baseEntitySaving.getBaseValue(attrName);
            BaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attrName);

            // если по атрибуту не было и нет значения то и делать дальше нечего, просто игнорируем
            if (baseValueLoaded == null && baseValueSaving == null)
                continue;

            final MetaAttribute metaAttribute = metaClass.getMetaAttribute(attrName);
            final MetaType metaType = metaAttribute.getMetaType();

            // обогащаем сущность: если атрибут не пришел в xml но у нас есть значение ранее подгруженное
            if (baseValueSaving == null && baseValueLoaded != null && !metaAttribute.isFinal())
                baseEntityApplied.put(attrName, baseValueLoaded);

            if (baseValueSaving == null)
                continue;

            if (metaType.isComplex()) {
                if (metaType.isSet()) {
                    BaseSet childBaseSetApplied = processComplexSet(metaAttribute, baseValueSaving, baseValueLoaded, baseEntityManager);
                    baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseSetApplied));
                }
                else
                    processComplexValue(baseEntitySaving, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            } else {
                if (metaType.isSet()) {
                    BaseSet childBaseSetApplied = processSimpleSet(baseValueSaving, baseValueLoaded);
                    baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseSetApplied));
                }
                else
                    processSimpleValue(metaAttribute, baseEntityApplied, baseEntityLoaded, baseValueSaving, baseValueLoaded);
            }
        }

        // сверяем отчетные даты сущности чтобы определить какую операцию проводить
        // если запись за отчетный период уже существует то делаем просто update, иначе по сущности добавляем новую историю
        // чтобы в холостую не выполнять операций в БД по сущности, сверяем имеющиеся атрибуты сущности из БД с атрибутами которые прислали;
        // если значения атрибутов фактический поменялись то помечаем сущность на обработку
        boolean baseEntityChanged = markBaseEntityChanges(baseEntityApplied, baseEntityLoaded);

        if (compareDates != 0) {
            if (baseEntityChanged)
                baseEntityManager.registerAsNewHistory(baseEntityApplied);
            // если же не было измений и прислали данные задней датой то сдвигаем отчетную дату сущности
            else if (compareDates < 0) {
                baseEntityApplied.setOldReportDate(baseEntityLoaded.getReportDate());
                baseEntityManager.registerAsShift(baseEntityApplied);
            }
        }
        else if (compareDates == 0 && baseEntityChanged)
            baseEntityManager.registerAsUpdated(baseEntityApplied);

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    /**
     * метод сверяет имеющуюся атрибуты сущности из БД с атрибутами которые прислали
     * если значение атрибута поменялось то метод помечает его как измененный
     * */
    private boolean markBaseEntityChanges(final BaseEntity baseEntityApplied, final BaseEntity baseEntityLoaded) {
        boolean changed = false;

        for (String attrName : baseEntityApplied.getAttributeNames()) {
            BaseValue childBaseValueLoaded = baseEntityLoaded.getBaseValue(attrName);
            BaseValue childBaseValueApplied = baseEntityApplied.getBaseValue(attrName);

            childBaseValueApplied.setChanged(Boolean.FALSE);

            if (childBaseValueLoaded == null ||
                (childBaseValueApplied.getValue() != null && childBaseValueLoaded.getValue() == null) ||
                (childBaseValueApplied.getValue() == null && childBaseValueLoaded.getValue() != null) ||
                (!childBaseValueApplied.equalsByValue(childBaseValueLoaded))) {
                childBaseValueApplied.setChanged(Boolean.TRUE);
                changed = true;
            }
        }

        return changed;
    }

    //TODO: не полностью реализован
    //TODO: черновой вариант
    private void processSimpleValue(MetaAttribute metaAttribute, BaseEntity baseEntityApplied, BaseEntity baseEntityLoaded,
                                    BaseValue baseValueSaving, BaseValue baseValueLoaded) {
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
                if (!metaAttribute.isFinal() && compareDates > 0)
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

    private void processComplexValue(BaseEntity baseEntitySaving, BaseEntity baseEntityApplied, BaseValue baseValueSaving,
                                     BaseValue baseValueLoaded, BaseEntityManager baseEntityManager) {
        MetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        MetaClass metaClass = baseEntityApplied.getMetaClass();

        BaseEntity childBaseEntityLoaded = null;
        if (baseValueLoaded != null)
            childBaseEntityLoaded = (BaseEntity) baseValueLoaded.getValue();

        BaseEntity childBaseEntitySaving = null;
        if (baseValueSaving != null)
            childBaseEntitySaving = (BaseEntity) baseValueSaving.getValue();

        if (metaAttribute.isImmutable() && childBaseEntitySaving.getId() == null)
            throw new IllegalStateException(Errors.compose(Errors.E23, childBaseEntitySaving));

        if (metaAttribute.isImmutable()) {
            baseEntityApplied.put(metaAttribute.getName(), baseValueSaving);
            return;
        }

        LocalDate reportDateLoaded = childBaseEntityLoaded.getReportDate();
        LocalDate reportDateSaving = baseEntityApplied.getReportDate();

        int compareDates = reportDateSaving.compareTo(reportDateLoaded);

        BaseEntity childBaseEntityApplied = null;
        if (baseValueSaving != null && baseValueSaving.getValue() != null) {
            // TODO: возможно необходимо вызывать processExistingBaseEntity
            childBaseEntityApplied = processBaseEntity(childBaseEntitySaving, null, baseEntityManager);
        }

        if (baseValueLoaded != null) {
            if (baseValueSaving == null) {
                // в случае если атрибут не пришел в теге но за другой период у нас есть значение
                // обогащаем сущность другим значением
                if (!metaAttribute.isFinal() && compareDates > 0) {
                    baseEntityApplied.put(metaAttribute.getName(), new BaseValue(baseValueLoaded.getValue()));
                }
            }
            else {
                // если ранее было значение и теперь пришло новое значение то просто заменяем новым
                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseEntityApplied));
            }
        }
        // если ранее у сущности не было значения по атрибуту
        else {
            if (baseValueSaving != null)
                baseEntityApplied.put(metaAttribute.getName(), new BaseValue(childBaseEntityApplied));
        }
    }

    private BaseSet processSimpleSet(BaseValue baseValueSaving, BaseValue baseValueLoaded) {
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

        Set<UUID> processedUUIds = new HashSet<>();

        for (BaseValue childBaseValueSaving : childBaseSetSaving.getValues()) {
            boolean baseValueFound = false;

            if (childBaseSetLoaded != null) {
                for (BaseValue childBaseValueLoaded : childBaseSetLoaded.getValues()) {
                    if (processedUUIds.contains(childBaseValueLoaded.getUuid()))
                        continue;

                    if (childBaseValueSaving.equalsByValue(childBaseValueLoaded)) {
                        processedUUIds.add(childBaseValueLoaded.getUuid());
                        childBaseSetApplied.put(new BaseValue(childBaseValueSaving.getValue()));
                        baseValueFound = true;
                        break;
                    }
                }
            }

            if (!baseValueFound)
                childBaseSetApplied.put(new BaseValue(childBaseValueSaving.getValue()));
        }

        // кумулятивный сет если даже значение не пришло в батче то все равно дополняем сет из ранее загруженных
        if (childBaseSetLoaded != null && metaAttribute.isCumulative()) {
            childBaseSetLoaded.getValues().stream()
                .filter(childBaseValueLoaded -> !processedUUIds.contains(childBaseValueLoaded.getUuid()))
                .forEach(childBaseValueLoaded -> childBaseSetApplied.put(new BaseValue(childBaseValueLoaded.getValue())));
        }

        return childBaseSetApplied;
    }

    //TODO: черновой вариант
    private BaseSet processComplexSet(MetaAttribute metaAttribute, BaseValue baseValueSaving, BaseValue baseValueLoaded, BaseEntityManager baseEntityManager) {
        if (baseValueSaving == null)
            throw new IllegalArgumentException("Атрибут пустой");

        MetaType metaType = metaAttribute.getMetaType();

        MetaSet childMetaSet = (MetaSet) metaType;
        MetaType childMetaType = childMetaSet.getMetaType();
        MetaClass childMetaClass = (MetaClass) childMetaType;

        BaseSet childBaseSetSaving = null;
        if (baseValueSaving.getValue() != null)
            childBaseSetSaving = (BaseSet) baseValueSaving.getValue();

        BaseSet childBaseSetLoaded = null;
        if (baseValueLoaded != null && baseValueLoaded.getValue() != null)
            childBaseSetLoaded = (BaseSet) baseValueLoaded.getValue();

        BaseSet childBaseSetApplied = new BaseSet(childMetaClass);

        Set<UUID> processedUUIds = new HashSet<>();

        if (childBaseSetSaving != null) {
            for (BaseValue childBaseValueSaving : childBaseSetSaving.getValues()) {
                BaseEntity childBaseEntitySaving = (BaseEntity) childBaseValueSaving.getValue();
                BaseEntity childBaseEntityApplied = null;

                if (childBaseSetLoaded != null && childBaseEntitySaving.getId() != null) {
                    for (BaseValue childBaseValueLoaded : childBaseSetLoaded.getValues()) {
                        if (processedUUIds.contains(childBaseValueLoaded.getUuid()))
                            continue;

                        BaseEntity childBaseEntityLoaded = (BaseEntity) childBaseValueLoaded.getValue();
                        if (childBaseEntityLoaded.getId().equals(childBaseEntitySaving.getId())) {
                            processedUUIds.add(childBaseValueLoaded.getUuid());

                            // ранее загруженная сущность, отправляем ее на обработку
                            childBaseEntityApplied = processExistingBaseEntity(metaAttribute, childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager);
                            childBaseSetApplied.put(new BaseValue(childBaseEntityApplied));
                            break;
                        }
                    }
                }

                if (childBaseEntityApplied == null) {
                    childBaseEntityApplied = processBaseEntity(childBaseEntitySaving, null, baseEntityManager);
                    childBaseSetApplied.put(new BaseValue(childBaseEntityApplied));
                }
            }
        }

        if (childBaseSetLoaded != null) {
            for (BaseValue childBaseValueLoaded : childBaseSetLoaded.getValues()) {
                BaseEntity childBaseEntityLoaded =  (BaseEntity) childBaseValueLoaded.getValue();
                if (processedUUIds.contains(childBaseValueLoaded.getUuid()))
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

    /**
     * метод делает insert сущности в таблицы схемы EAV_XML
     * @param baseEntitySaving должен быть корень дерева и все сущности должны иметь ID. Например кредит является корнем дерева.
     * был выбран подход обхода дерева в ширину так как заливка в схему EAV_XML происходит
     * после заливки в схему EAV_DATA и всем сущностям уже был присвоен id
     * то есть нам абсолюто не важно в каком порядке мы обрабатываем сущности
     * */
    @Override
    public void storeBaseEntityToSchemaEavXml(final BaseEntity baseEntitySaving) {
        if (baseEntitySaving.getParent() != null)
            throw new IllegalArgumentException("Сущность должна быть корнем дерева");

        Queue<BaseEntity> baseEntityQueue = new LinkedList<>();
        baseEntityQueue.add(baseEntitySaving);

        while (baseEntityQueue.size() > 0) {
            BaseEntity queuedBaseEntity = baseEntityQueue.poll();

            insertBaseEntityToDb(EavDbSchema.EAV_XML, queuedBaseEntity);

            for (String attribute : queuedBaseEntity.getAttributeNames()) {
                MetaAttribute metaAttribute = queuedBaseEntity.getMetaAttribute(attribute);
                MetaType metaType = metaAttribute.getMetaType();

                BaseValue baseValue = queuedBaseEntity.getBaseValue(attribute);
                if (!(baseValue != null && baseValue.getValue() != null && metaType.isComplex() && !metaAttribute.isImmutable()))
                    continue;

                MetaType childMetaType = metaAttribute.getMetaType();

                if (childMetaType.isSet()) {
                    BaseSet childBaseSet = (BaseSet) baseValue.getValue();
                    for (BaseValue childBaseValue : childBaseSet.getValues())
                        baseEntityQueue.add((BaseEntity) childBaseValue.getValue());
                } else {
                    baseEntityQueue.add((BaseEntity) baseValue.getValue());
                }
            }
        }
    }

    /**
     * метод делает insert сущности в базу => одна сущность = одна запись в базе
     * наименование таблицы и столбцов берет из мета данных
     * в любой таблице есть обязательные поля (см. код) помимо атрибутов мета класса
     * */
    private void insertBaseEntityToDb(final EavDbSchema schema, final BaseEntity baseEntitySaving) {
        if (baseEntitySaving.getId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id ");

        MetaClass metaClass = baseEntitySaving.getMetaClass();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(npJdbcTemplate.getJdbcTemplate());
        simpleJdbcInsert.setSchemaName(schema.name());
        simpleJdbcInsert.setTableName(metaClass.getTableName());

        // обязательные поля в реляционной таблице: ENTITY_ID, CREDITOR_ID, REPORT_DATE, BATCH_ID
        Set<String> columns = new HashSet<>(Arrays.asList("ENTITY_ID", "CREDITOR_ID", "REPORT_DATE", "BATCH_ID"));
        columns.addAll(baseEntitySaving.getAttributeNames().stream()
                .map(attributeName -> baseEntitySaving.getMetaAttribute(attributeName).getColumnName())
                .collect(Collectors.toList()));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("BATCH_ID", baseEntitySaving.getBatchId());
        params.addValue("ENTITY_ID", baseEntitySaving.getId());
        params.addValue("CREDITOR_ID", baseEntitySaving.getRespondentId());
        params.addValue("REPORT_DATE", Converter.convertToSqlDate(baseEntitySaving.getReportDate()));

        for (String attribute: baseEntitySaving.getAttributeNames()) {
            MetaAttribute metaAttribute = baseEntitySaving.getMetaAttribute(attribute);
            BaseValue baseValue = baseEntitySaving.getBaseValue(attribute);

            Object sqlValue = convertBaseValueToRelModel(schema.name(), metaAttribute, baseValue);
            params.addValue(metaAttribute.getColumnName(), sqlValue);
        }

        simpleJdbcInsert.setColumnNames(new ArrayList<>(columns));

        int count = simpleJdbcInsert.execute(params);
        if (count == 0)
            throw new IllegalArgumentException("Ошибка завершения DML операций по таблице " + String.join(".", metaClass.getSchemaXml(), metaClass.getTableName()));
    }

    /**
     * метод делает update сущности в БД (принцип => одна сущность = одна запись в базе)
     * @param baseEntitySaving означает если мы хотим только обновить отчетную дату сущности
     * необходимо передавать сущность лишь в случае если есть атрибуты которые изменились, иначе метод выбрасывает исключение.
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
     * если параметр baseEntitySaving = true то метод генерирует аналог SQL запроса:
     * update EAV_DATA.PLEDGE
     *   set BATCH_ID = 45,
     *       REPORT_DATE = :NEW_REPORT_DATE
     *  where CREDITOR_ID = :CREDITOR_ID
     *    and ENTITY_ID = :ENTITY_ID
     *    and REPORT_DATE = :REPORT_DATE
     * */
    private void updateBaseEntityInDb(final BaseEntity baseEntitySaving, boolean shiftReportDate) {
        if (baseEntitySaving.getId() == null)
            throw new IllegalArgumentException("Ошибка отсутствия у сущности id");
        if (baseEntitySaving.getValueCount() == 0)
            throw new IllegalArgumentException("У сущности отсутствуют значения");

        long changedAttributes = baseEntitySaving.getValues().stream().filter(BaseValue::isChanged).count();

        if (shiftReportDate) {
            if (baseEntitySaving.getOldReportDate() == null)
                throw new IllegalArgumentException("Отсутствует старая отчетная дата у сущности");
            else {
                int compareDates = baseEntitySaving.getReportDate().compareTo(baseEntitySaving.getOldReportDate());
                if (compareDates >= 0)
                    throw new IllegalArgumentException("Новая отчетная дата сущности дожна быть меньше старой");
            }

            if (changedAttributes > 0)
                throw new IllegalArgumentException("Есть измененные атрибутов у сущности");
        }
        else {
            if (baseEntitySaving.getOldReportDate() != null)
                throw new IllegalArgumentException("Присутствует старая отчетная дата у сущности");
            if (changedAttributes == 0)
                throw new IllegalArgumentException("Нет измененных атрибутов у сущности");
        }

        MetaClass metaClass = baseEntitySaving.getMetaClass();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("BATCH_ID", baseEntitySaving.getBatchId());
        params.addValue("ENTITY_ID", baseEntitySaving.getId());
        params.addValue("CREDITOR_ID", baseEntitySaving.getRespondentId());

        if (shiftReportDate) {
            params.addValue("NEW_REPORT_DATE", Converter.convertToSqlDate(baseEntitySaving.getReportDate()));
            params.addValue("REPORT_DATE", Converter.convertToSqlDate(baseEntitySaving.getOldReportDate()));
        }
        else
            params.addValue("REPORT_DATE", Converter.convertToSqlDate(baseEntitySaving.getReportDate()));

        StringBuilder fixedColumns = new StringBuilder();
        fixedColumns.append("BATCH_ID = :BATCH_ID\n");

        StringBuilder varColumns = new StringBuilder();

        if (!shiftReportDate) {
            for (String attrName : baseEntitySaving.getAttributeNames()) {
                MetaAttribute metaAttribute = baseEntitySaving.getMetaAttribute(attrName);
                String columnName = metaAttribute.getColumnName();

                BaseValue baseValue = baseEntitySaving.getBaseValue(attrName);
                if (baseValue == null || !baseValue.isChanged())
                    continue;

                Object value = null;
                if (baseValue.getValue() != null)
                    value = convertBaseValueToRelModel(metaClass.getSchemaData(), metaAttribute, baseValue);

                params.addValue(columnName, value);

                varColumns.append(",\n");
                varColumns.append(columnName).append(" = :").append(columnName);
            }
        }

        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ")
                .append(metaClass.getSchemaData())
                .append(".")
                .append(metaClass.getTableName()).append("\n");

        updateQuery.append("set ");
        updateQuery.append(fixedColumns);

        if (shiftReportDate)
            updateQuery.append("REPORT_DATE = :NEW_REPORT_DATE");
        else
            updateQuery.append(varColumns).append("\n");

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
    private Object convertBaseValueToRelModel(final String schema, final MetaAttribute metaAttribute, final BaseValue baseValue) {
        MetaType metaType = metaAttribute.getMetaType();

        Object value;
        if (metaType.isSet()) {
            Object array;
            BaseSet childBaseSet = (BaseSet) baseValue.getValue();
            if (metaType.isComplex())
                array = childBaseSet.getValues().stream()
                        .map(childBaseValue -> ((BaseEntity) childBaseValue.getValue()).getId())
                        .distinct().toArray();
            else
                array = childBaseSet.getValues().stream()
                        .map(childBaseValue -> ((BaseValue) childBaseValue.getValue()).getValue())
                        .distinct().toArray();

            try {
                // особенность Oracle, для создания массива обязательно пользоваться createARRAY а не createArrayOf
                // также необходимо получить соединение с базой spring утилитой иначе получим только прокси обьект
                Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                OracleConnection oraConn = conn.unwrap(OracleConnection.class);

                value = oraConn.createARRAY(String.join(".", schema, metaAttribute.getColumnType()), array);
            } catch (SQLException e) {
                // ловим exception и конвертируем в unchecked чтобы везде не добавлять try catch
                throw new UsciException(e.getMessage());
            }
        } else if (metaType.isComplex())
            value = ((BaseEntity) baseValue.getValue()).getId();
        else
            value = MetaDataType.convertToRmValue(((MetaValue) metaAttribute.getMetaType()).getMetaDataType(), baseValue.getValue());

        return value;

    }

}
