package kz.bsbnb.usci.core.model;

import java.io.Serializable;

public class EavHub implements Serializable {
    private Long respondentId;
    private String entityKey;
    private Long metaClassId;
    private Long entityId;
    private Long batchId;

    public EavHub() {
        /*An empty constructor*/
    }

    public EavHub(Long respondentId, String entityKey, Long metaClassId, Long batchId) {
        this.respondentId = respondentId;
        this.entityKey = entityKey;
        this.metaClassId = metaClassId;
        this.batchId = batchId;
    }

    public EavHub(Long id, Long respondentId, String entityKey, Long metaClassId, Long batchId) {
        this.entityId = id;
        this.respondentId = respondentId;
        this.entityKey = entityKey;
        this.metaClassId = metaClassId;
        this.batchId = batchId;
    }

    //region Getters and Setters

    public Long getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }

    public String getEntityKey() {
        return entityKey;
    }

    public void setEntityKey(String entityKey) {
        this.entityKey = entityKey;
    }

    public Long getMetaClassId() {
        return metaClassId;
    }

    public void setMetaClassId(Long metaClassId) {
        this.metaClassId = metaClassId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }


    //endregion

}
