package kz.bsbnb.usci.model.eav.data;


import java.util.*;

import kz.bsbnb.usci.model.eav.meta.MetaClass;

public class DataEntity implements DataContainer {

    private MetaClass meta;

    private DataOperationType dataOperationType;

    private HashMap<String, DataValue> values = new HashMap<>();


    public DataOperationType getOperation() {
        return dataOperationType;
    }

    public void setOperation(DataOperationType type) {
        dataOperationType = type;
    }

    public DataEntity() {
        super();
    }

    public MetaClass getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public String toString() {
        return null;
    }


    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += 31 * result + meta.hashCode();
        result += 31 * result + values.hashCode();

        return result;
    }

    @Override
    public DataEntity clone() {

        return null;
    }

    @Override
    public boolean isSet() {
        // TODO Implement this method
        return false;
    }

    @Override
    public boolean isComplex() {
        // TODO Implement this method
        return false;
    }
    
}


