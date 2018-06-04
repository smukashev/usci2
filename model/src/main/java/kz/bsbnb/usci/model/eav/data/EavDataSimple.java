package kz.bsbnb.usci.model.eav.data;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public class EavDataSimple implements EavData {
    private EavData dataContainer;
    private MetaAttribute metaAttribute;
    private EavDataSimple newEavDataSimple = null;
    private Object value;

    public EavDataSimple() {
        /*An empty constructor*/
    }

    public EavDataSimple(Object value) {
        this.value = value;
    }

    @Override
    public EavData getDataContainer() {
        return dataContainer;
    }

    @Override
    public void setDataContainer(EavData dataContainer) {
        this.dataContainer = dataContainer;
    }

    @Override
    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    @Override
    public void setMetaAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setNewEavDataSimple(EavDataSimple eavDataSimple) {
        this.newEavDataSimple = eavDataSimple;
    }

    public EavDataSimple getNewEavDataSimple() {
        return newEavDataSimple;
    }

    @Override
    public void setMetaType(MetaType metaType) {

    }

    @Override
    public MetaType getMetaType() {
        return null;
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return false;
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
            EavDataSimple that = (EavDataSimple) obj;
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
    public EavDataSimple clone() {
        return null;
    }

    @Override
    public String toString() {
            return null;
    }

}


