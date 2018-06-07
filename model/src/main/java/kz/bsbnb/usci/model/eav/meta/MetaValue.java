package kz.bsbnb.usci.model.eav.meta;

public class MetaValue implements MetaType {
    private MetaDataType metaDataType;

    public MetaValue() {
        /*An empty constructor*/
    }

    public MetaValue(MetaDataType metaDataType) {
        this.metaDataType = metaDataType;
    }

    public MetaDataType getMetaDataType() {
        return metaDataType;
    }

    public void setMetaDataType(MetaDataType type) {
        this.metaDataType = type;
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
            return tmp.getMetaDataType() == this.getMetaDataType();
        }
    }

    public int hashCode() {
        return metaDataType.hashCode();
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String prefix) {
        return "metaValue: " + metaDataType;
    }

}

