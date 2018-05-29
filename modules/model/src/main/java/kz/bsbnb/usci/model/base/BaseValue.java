package kz.bsbnb.usci.model.base;

import kz.bsbnb.usci.model.Persistable;
import kz.bsbnb.usci.model.meta.MetaAttribute;

public class BaseValue extends Persistable {


    private BaseContainer baseContainer;

    private MetaAttribute metaAttribute;

    private BaseValue newBaseValue = null;

    private Object value;

    public BaseValue() {
        super();
    }

    public BaseContainer getBaseContainer() {
        return baseContainer;
    }

    public void setBaseContainer(BaseContainer baseContainer) {
        this.baseContainer = baseContainer;
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

    public void setNewBaseValue(BaseValue baseValue) {
        this.newBaseValue = baseValue;
    }

    public BaseValue getNewBaseValue() {
        return newBaseValue;
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
            return null;
    }

}


