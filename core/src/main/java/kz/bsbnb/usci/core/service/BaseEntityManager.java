package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseValue;

import java.util.*;

public class BaseEntityManager {
    private Long respondentId;

    private List<BaseEntity> hubs = new ArrayList<>();
    private List<BaseEntity> insertedEntities = new ArrayList<>();
    private List<BaseEntity> updatedEntities = new ArrayList<>();
    private List<BaseEntity> newHistoryEntities = new ArrayList<>();
    private Map<String, List<BaseEntity>> deletedEntities = new HashMap<>();
    private Map<String, List<BaseEntity>> processedEntities = new HashMap<>();

    private Map<String, BaseEntity> baseEntityPairs = new HashMap<>();

    private void registerEntity(Map<String, List<BaseEntity>> objects, BaseEntity baseEntity) {
        String metaClassNme = baseEntity.getMetaClass().getClassName();

        if (objects.containsKey(metaClassNme)) {
            objects.get(metaClassNme).add(baseEntity);
        } else {
            List<BaseEntity> objList = new ArrayList<>();
            objList.add(baseEntity);

            objects.put(metaClassNme, objList);
        }
    }

    public void registerAsInserted(BaseEntity insertedEntity) {
        if (insertedEntity == null)
            throw new RuntimeException(Errors.compose(Errors.E54));

        insertedEntities.add(insertedEntity);
    }

    public void registerAsUpdated(BaseEntity updatedEntity) {
        if (updatedEntity == null)
            throw new RuntimeException(Errors.compose(Errors.E54));

        updatedEntities.add(updatedEntity);
    }

    public void registerAsNewHistory(BaseEntity newHistory) {
        if (newHistory == null)
            throw new RuntimeException(Errors.compose(Errors.E54));

        newHistoryEntities.add(newHistory);
    }

    public void registerAsDeleted(BaseEntity deletedEntity) {
        if (deletedEntity == null)
            throw new RuntimeException(Errors.compose(Errors.E53));

        List<BaseEntity> objList = deletedEntities.get(deletedEntity.getMetaClass().getClassName());

        if(objList != null && deletedEntity.getId() > 0) {
            for (BaseEntity baseEntity : objList)
                if (baseEntity.getId() > 0 && baseEntity.getId() == deletedEntity.getId())
                    return;
        }

        registerEntity(deletedEntities, deletedEntity);
    }

    public void registerProcessedBaseEntity(BaseEntity processedBaseEntity) {
        List<BaseEntity> entityList = processedEntities.get(processedBaseEntity.getMetaClass().getClassName());
        if (entityList == null)
            entityList = new ArrayList<>();

        entityList.add(processedBaseEntity);
        processedEntities.put(processedBaseEntity.getMetaClass().getClassName(), entityList);
    }

    public void addOptimizerEntity(BaseEntity entity) {
        hubs.add(entity);
    }

    public BaseEntity getProcessed(BaseEntity baseEntity) {
        if (!baseEntity.getMetaClass().isSearchable())
            return null;

        List<BaseEntity> entityList = processedEntities.get(baseEntity.getMetaClass().getClassName());
        if (entityList == null)
            return null;

        for (BaseEntity currentBaseEntity : entityList)
            if (baseEntity.equalsByKey(currentBaseEntity))
                return currentBaseEntity;

        return null;
    }

    public void registerRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }

    public Long getRespondentId() {
        return respondentId;
    }

    public List<BaseEntity> getInsertedEntities() {
        return insertedEntities;
    }

    public List<BaseEntity> getNewHistoryEntities() {
        return newHistoryEntities;
    }

    public List<BaseEntity> getUpdatedEntities() {
        return updatedEntities;
    }

    public void saveBaseEntitySavingAppliedPair(BaseEntity baseEntitySaving, BaseEntity baseEntityApplied) {
        baseEntityPairs.put(baseEntityApplied.getUuid().toString(), baseEntitySaving);
    }

    public Map<String, BaseEntity> getBaseEntityPairs() {
        return baseEntityPairs;
    }
}
