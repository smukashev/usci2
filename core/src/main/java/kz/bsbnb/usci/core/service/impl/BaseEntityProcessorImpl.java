package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.UsciException;
import kz.bsbnb.usci.model.eav.base.*;
import kz.bsbnb.usci.model.eav.meta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author BSB
 */

@Service
public class BaseEntityProcessorImpl implements BaseEntityProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorImpl.class);

    private final EavHubService eavHubService;
    private final BaseEntityStoreService baseEntityStoreService;

    public BaseEntityProcessorImpl(EavHubService eavHubService, BaseEntityStoreService baseEntityStoreService) {
        this.eavHubService = eavHubService;
        this.baseEntityStoreService = baseEntityStoreService;
    }

    @Override
    @Transactional
    public BaseEntity processBaseEntity(final BaseEntity baseEntitySaving, LocalDate reportDate) {
        BaseEntityManager baseEntityManager = new BaseEntityManager();

        BaseEntity baseEntityPrepared;
        BaseEntity baseEntityApplied;

        // все сущности кроме справочников должны иметь респондента
        if (!baseEntitySaving.getMetaClass().isDictionary() && baseEntitySaving.getRespondentId() == null)
            throw new IllegalStateException(Errors.compose(Errors.E197));

        baseEntityManager.registerRespondentId(baseEntitySaving.getRespondentId());

        baseEntityPrepared = prepareBaseEntity(baseEntitySaving.clone());

        //TODO: проверка сущности на бизнес правила
        //временно отключен так как еще не реализован

        switch (baseEntityPrepared.getOperation()) {
            case INSERT:
                if (baseEntityPrepared.getId() != null)
                    throw new UsciException(Errors.compose(Errors.E196, baseEntityPrepared.getId()));

                baseEntityApplied = baseEntityStoreService.processBaseEntity(baseEntityPrepared, null, baseEntityManager);
                baseEntityStoreService.storeBaseManager(baseEntityManager);

                // заливаем данные непосредственно в схему EAV_XML
                baseEntityStoreService.storeBaseEntityToSchemaEavXml(baseEntityPrepared);

                break;
            case UPDATE:
                if (baseEntityPrepared.getId() == null)
                    throw new UsciException(Errors.compose(Errors.E198));

                baseEntityApplied = baseEntityStoreService.processBaseEntity(baseEntityPrepared, null, baseEntityManager);
                baseEntityStoreService.storeBaseManager(baseEntityManager);

                // заливаем данные непосредственно в схему EAV_XML
                baseEntityStoreService.storeBaseEntityToSchemaEavXml(baseEntityPrepared);

                break;
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

    /**
     * метод выполняеет следующие задачи:
     * - поиск сущности и присовение ей ID:
     * алгоритм поиска:
     *  идет обход в глубину всего дерева, всех комплексных* атрибутов сущностей
     *  затем по ключевым атрибутам сущности происходит поиск в БД
     *  пример:
     *  кредит, залоги, субьект, справочники, документы подвергаются поиску
     * - проставляем родителя у дочерних узлов
     * */
    // TODO: черновой вариант
    public BaseEntity prepareBaseEntity(final BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMetaClass();

        // блок кода обрабатывает только комплексные атрибуты
        // отрабатывает рекурсивно потому что сущность может зависеть от других сущностей
        // найденный id сущности служит ключом у другой сущности
        // также проставляем родителя у дочерних узлов
        for (String attrName : baseEntity.getAttributeNames()) {
            MetaAttribute metaAttribute = metaClass.getMetaAttribute(attrName);
            MetaType metaType = metaAttribute.getMetaType();
            BaseValue baseValue = baseEntity.getBaseValue(attrName);

            if (metaType.isComplex() && baseValue.getValue() != null) {
                if (metaType.isSet()) {
                    BaseSet childBaseSet = (BaseSet) baseValue.getValue();
                    for (BaseValue childBaseValue : childBaseSet.getValues()) {
                        BaseEntity childBaseEntity = (BaseEntity) childBaseValue.getValue();
                        if (childBaseEntity.getValueCount() != 0) {
                            // заполняем у дочерних узлов(кроме ключевых) признак родителя
                            childBaseEntity.setParent(baseEntity, metaAttribute);

                            prepareBaseEntity((BaseEntity) childBaseValue.getValue());
                        }
                    }
                }
                else {
                    BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();
                    if (childBaseEntity.getValueCount() != 0) {
                        // заполняем у дочерних узлов(кроме ключевых) признак родителя
                        childBaseEntity.setParent(baseEntity, metaAttribute);

                        prepareBaseEntity(childBaseEntity);
                    }
                }
            }
        }

        if (metaClass.isSearchable() && baseEntity.getId() == null)
            baseEntity.setId(searchBaseEntity(baseEntity));

        //TODO: parentIsKey проблему решить
        if (metaClass.parentIsKey() && baseEntity.getId() == null)
            baseEntity.setId(searchBaseEntity(baseEntity));

        return baseEntity;
    }

    // TODO: поиск сущности пока все нюансы не учитывает
    // TODO: черновой вариант
    private Long searchBaseEntity(BaseEntity baseEntity) {
        return eavHubService.find(baseEntity);
    }

}
