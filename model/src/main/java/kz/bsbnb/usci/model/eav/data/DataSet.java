package kz.bsbnb.usci.model.eav.data;

import java.util.HashSet;

import java.util.Set;

import kz.bsbnb.usci.model.eav.meta.MetaType;

public class DataSet implements DataContainer {

    private MetaType metaType;

    private Set<DataValue> values = new HashSet<>();

    public DataSet() {
        super();
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public void add(DataValue value) {
        value.setDataContainer(this);
        values.add(value);
    }

    public void remove(DataValue value) {
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

    public DataSet clone() {
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
