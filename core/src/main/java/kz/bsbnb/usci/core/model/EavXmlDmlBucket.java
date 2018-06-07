package kz.bsbnb.usci.core.model;

import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseType;

import java.util.List;

//TODO: пока что черновой вариант
public class EavXmlDmlBucket implements DmlBucket {
    private List<BaseEntity> entities;

    public EavXmlDmlBucket() {
        /**/
    }

    public List<BaseEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<BaseEntity> entities) {
        this.entities = entities;
    }

    @Override
    public void registerAsInserted(BaseType persistableObject) {

    }

    @Override
    public void registerAsUpdated(BaseType persistableObject) {

    }

    @Override
    public void registerAsDeleted(BaseType persistableObject) {

    }

}
