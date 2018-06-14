package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaDataType;

import java.time.LocalDate;

/**
 * @author BSB
 */

public class BaseValue implements Cloneable {
    private BaseContainer baseContainer;
    private MetaAttribute metaAttribute;
    private Object newValue = null;
    private Object value;

    public BaseValue() {
        /*Пустой конструктор*/
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
        if (this == obj)
            return true;

        if (obj == null || (getClass() != obj.getClass()))
            return false;

        BaseValue that = (BaseValue) obj;
        return value != null ? value.equals(that.value) : that.value == null;
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
        BaseValue baseValue;
        try {
            baseValue = (BaseValue) super.clone();

            if (value != null) {
                if (value instanceof BaseEntity)
                    baseValue.setValue(((BaseEntity) value).clone());
                if (value instanceof BaseSet)
                    baseValue.setValue(((BaseSet) value).clone());
            }
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(Errors.compose(Errors.E37));
        }
        return baseValue;
    }

    @Override
    public String toString() {
        if (value == null)
            return null;

        return value.toString();
    }

}


