package kz.bsbnb.usci.model.eav.meta;

public class MetaSet implements MetaType {
    MetaType metaType;

    public MetaSet() {
        super();
    }

    @Override
    public boolean isSet() {
        return true;
    }

    @Override
    public boolean isComplex() {
        return metaType.isComplex();
    }

    public void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }

    public MetaType getMetaType() {
        return metaType;
    }
    
    @Override
    public String toString() {
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaSet)) return false;

        MetaSet metaSet = (MetaSet) o;

        if (!metaType.equals(metaSet.metaType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metaType.hashCode();
        result = 31 * result;
        return result;
    }

}
