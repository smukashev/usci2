package kz.bsbnb.usci.model.base;

import java.util.HashSet;

import java.util.Set;

import kz.bsbnb.usci.model.meta.MetaType;

public class BaseSet implements BaseContainer {

    private MetaType metaType;

    private Set<BaseValue> values = new HashSet<>();

    public BaseSet() {
        super();
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public void add(BaseValue value) {
        value.setBaseContainer(this);
        values.add(value);
    }

    public void remove(BaseValue value) {
        values.remove(value);
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

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
    
}
