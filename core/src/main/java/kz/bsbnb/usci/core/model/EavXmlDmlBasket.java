package kz.bsbnb.usci.core.model;

import kz.bsbnb.usci.model.eav.data.BaseEntity;

import java.util.List;

public class EavXmlDmlBasket {
    private List<BaseEntity> entities;

    public EavXmlDmlBasket() {
        /**/
    }

    public List<BaseEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<BaseEntity> entities) {
        this.entities = entities;
    }
}
