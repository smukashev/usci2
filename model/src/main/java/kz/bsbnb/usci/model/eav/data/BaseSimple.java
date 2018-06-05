package kz.bsbnb.usci.model.eav.data;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaDataType;
import kz.bsbnb.usci.model.eav.meta.MetaType;
import kz.bsbnb.usci.model.eav.meta.MetaValue;

public class BaseSimple implements BaseType {
    private BaseType baseContainer;
    private MetaAttribute metaAttribute;
    private Object newValue = null;
    private Object value;

    public BaseSimple() {
        /*An empty constructor*/
    }

    public BaseSimple(Object value) {
        this.value = value;
    }

    @Override
    public BaseType getBaseContainer() {
        return baseContainer;
    }

    @Override
    public void setBaseContainer(BaseType baseContainer) {
        this.baseContainer = baseContainer;
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

    public void setNewValue(Object baseSimple) {
        this.newValue = baseSimple;
    }

    public Object getNewValue() {
        return newValue;
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
            BaseSimple that = (BaseSimple) obj;
            return value != null ? value.equals(that.value) : that.value == null;
        }
    }

    public Object getRmValue() {
        return MetaDataType.convertToRmValue(((MetaValue) metaAttribute.getMetaType()).getMetaDataType(), value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public BaseSimple clone() {
        return null;
    }

    @Override
    public String toString() {
            return null;
    }

}


