package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaDataType;

/**
 * @author BSB
 */

public class BaseValue /*implements BaseType*/ {
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

    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public void setMetaAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
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

    public boolean equalsToString(String str, MetaDataType type) {
        switch (type) {
            case INTEGER:
                if (value.equals(Integer.parseInt(str)))
                    return true;
                break;
            case DATE:
                throw new UnsupportedOperationException(Errors.compose(Errors.E41));
            case STRING:
                if (value.equals(str))
                    return true;
                break;
            case BOOLEAN:
                if (value.equals(Boolean.parseBoolean(str)))
                    return true;
                break;
            case DOUBLE:
                if (value.equals(Double.parseDouble(str)))
                    return true;
                break;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E7, type));
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public BaseValue clone() {
        //TODO: реализовать если есть необходимость или удалить
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        if (value == null)
            return null;

        return value.toString();
    }

}


