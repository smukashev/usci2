package kz.bsbnb.usci.model.eav.base;

import java.util.HashSet;

import java.util.Set;
import java.util.UUID;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public class BaseSet implements BaseContainer {
    private UUID uuid = UUID.randomUUID();

    private BaseContainer baseContainer;
    private MetaType metaType;
    private Set<BaseValue> values = new HashSet<>();

    public BaseSet() {
        /*An empty constructor*/
    }

    public BaseSet(MetaType metaType) {
        this.metaType = metaType;
    }

    @Override
    public MetaType getMetaType() {
        return metaType;
    }

    public void put(BaseValue value) {
        value.setBaseContainer(this);
        values.add(value);
    }

    public void remove(BaseValue value) {
        values.remove(value);
    }

    @Override
    public Set<BaseValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (BaseType value : values) {
            if (first) {
                sb.append(value.toString());
                first = false;
            } else {
                sb.append(", ").append(value.toString());
            }
        }

        sb.append("]");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public BaseSet clone() {
        return null;
    }

    @Override
    public boolean isSet() {
        return true;
    }

    @Override
    public boolean isComplex() {        
        return metaType.isComplex();
    }

    /*@Override
    public void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }*/

    /*@Override
    public void setBaseAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    @Override
    public MetaAttribute getBaseAttribute() {
        return metaAttribute;
    }*/

    @Override
    public BaseContainer getBaseContainer() {
        return baseContainer;
    }

    @Override
    public void setBaseContainer(BaseContainer baseContainer) {
        this.baseContainer = baseContainer;
    }

}
