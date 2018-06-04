package kz.bsbnb.usci.model.eav.meta;

public class MetaValue implements MetaType {
    private MetaDataType dataType;

    public MetaValue() {
        /*An empty constructor*/
    }

    public MetaValue(MetaDataType typeCode) {
        this.dataType = typeCode;
    }

    public MetaDataType getDataType() {
        return dataType;
    }

    public void setDataType(MetaDataType type) {
        this.dataType = type;
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return false;
    }
    
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;
        else {
            MetaValue tmp = (MetaValue) obj;
            return tmp.getDataType() == this.getDataType();
        }
    }

    public int hashCode() {
        return dataType.hashCode();
    }

    @Override
    public String toString() {
        return null;
    }

}

