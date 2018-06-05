package kz.bsbnb.usci.model.eav.data;

import java.util.HashSet;

import java.util.Set;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public class BaseSet implements BaseType {
    private BaseType dataContainer;
    private MetaType metaType;
    private MetaAttribute metaAttribute;
    private Set<BaseType> values = new HashSet<>();

    public BaseSet() {
        /*An empty constructor*/
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public void add(BaseType value) {
        //TODO: реализовать до конца
        value.setBaseContainer(this);
        values.add(value);
    }

    public void remove(BaseSimple value) {
        values.remove(value);
    }

    public Set<BaseType> getValues() {
        return values;
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

    @Override
    public void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }

    @Override
    public void setMetaAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    @Override
    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    @Override
    public BaseType getBaseContainer() {
        return dataContainer;
    }

    @Override
    public void setBaseContainer(BaseType baseContainer) {
        this.dataContainer = baseContainer;
    }

}
