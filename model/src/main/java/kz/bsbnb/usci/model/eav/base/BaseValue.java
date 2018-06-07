package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaDataType;
import kz.bsbnb.usci.model.eav.meta.MetaType;
import kz.bsbnb.usci.model.eav.meta.MetaValue;

public class BaseValue implements BaseType {
    private BaseContainer baseContainer;
    private MetaAttribute metaAttribute;
    private Object newValue = null;
    private Object value;

    public BaseValue() {
        /*An empty constructor*/
    }

    public BaseValue(Object value) {
        this.value = value;
    }

    public BaseContainer getBaseContainer() {
        return baseContainer;
    }

    public void setBaseContainer(BaseContainer baseContainer) {
        this.baseContainer = baseContainer;
    }

    /*@Override
    public MetaAttribute getBaseAttribute() {
        return metaAttribute;
    }

    @Override
    public void setBaseAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }*/

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

    /*@Override
    public void setMetaType(MetaType metaType) {

    }*/

    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public void setMetaAttribute(MetaAttribute baseAttribute) {
        this.metaAttribute = metaAttribute;
    }

    @Override
    public MetaType getMetaType() {
        return metaAttribute.getMetaType();
    }

    @Override
    public boolean isSet() {
        //TODO: необходимо удалить
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isComplex() {
        //TODO: необходимо удалить
        throw new UnsupportedOperationException();
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
            BaseValue that = (BaseValue) obj;
            return value != null ? value.equals(that.value) : that.value == null;
        }
    }

    /*public Object getRmValue() {
        //TODO: пока только для примитивных
        return MetaDataType.convertToRmValue(((MetaValue) metaAttribute.getMetaType()).getMetaDataType(), value);
    }*/

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public BaseValue clone() {
        return null;
    }

    @Override
    public String toString() {
        if (value == null)
            return null;

        return value.toString();
    }

}


