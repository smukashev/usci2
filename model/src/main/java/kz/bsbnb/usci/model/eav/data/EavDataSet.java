package kz.bsbnb.usci.model.eav.data;

import java.util.HashSet;

import java.util.Set;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public class EavDataSet implements EavData {
    private EavData dataConainer;
    private MetaType metaType;
    private MetaAttribute metaAttribute;
    private Set<EavData> values = new HashSet<>();

    public EavDataSet() {
        /*An empty constructor*/
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public void add(EavData value) {
        //TODO: реализовать до конца
        value.setDataContainer(this);
        values.add(value);
    }

    public void remove(EavDataSimple value) {
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

    public EavDataSet clone() {
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
    public EavData getDataContainer() {
        return dataConainer;
    }

    @Override
    public void setDataContainer(EavData dataContainer) {
        this.dataConainer = dataContainer;
    }

}
