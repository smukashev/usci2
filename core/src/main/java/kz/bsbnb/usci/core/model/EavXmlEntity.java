package kz.bsbnb.usci.core.model;

import kz.bsbnb.usci.model.eav.data.DataEntity;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;

import java.util.Map;

public class EavXmlEntity {
    private DataEntity dataEntity;
    private Map<MetaAttribute, Object> values;

    public EavXmlEntity() {
        /*An empty constructor*/
    }

    //region Getters and Setters
    public DataEntity getDataEntity() {
        return dataEntity;
    }

    public void setDataEntity(DataEntity dataEntity) {
        this.dataEntity = dataEntity;
    }

    public Map<MetaAttribute, Object> getValues() {
        return values;
    }

    public void setValues(Map<MetaAttribute, Object> values) {
        this.values = values;
    }
    //endregion
}
