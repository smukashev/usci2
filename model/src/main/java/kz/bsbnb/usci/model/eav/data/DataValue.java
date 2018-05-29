package kz.bsbnb.usci.model.eav.data;

import kz.bsbnb.usci.model.Persistable;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;

public class DataValue extends Persistable {


    private DataContainer dataContainer;

    private MetaAttribute metaAttribute;

    private DataValue newDataValue = null;

    private Object value;

    public DataValue() {
        super();
    }

    public DataContainer getDataContainer() {
        return dataContainer;
    }

    public void setDataContainer(DataContainer dataContainer) {
        this.dataContainer = dataContainer;
    }

    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public void setMetaAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setNewDataValue(DataValue dataValue) {
        this.newDataValue = dataValue;
    }

    public DataValue getNewDataValue() {
        return newDataValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;
        else {
            DataValue that = (DataValue) obj;
            return value != null ? value.equals(that.value) : that.value == null;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public DataValue clone() {
        return null;
    }

    @Override
    public String toString() {
            return null;
    }

}


