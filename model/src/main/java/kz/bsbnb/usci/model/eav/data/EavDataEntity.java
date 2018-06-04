package kz.bsbnb.usci.model.eav.data;

import java.time.LocalDate;
import java.util.*;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.*;

/**
 * @author BSB
 */

public class EavDataEntity implements EavData {
    private MetaClass metaClass;
    private LocalDate reportDate;
    private EavData dataContainer;
    private MetaAttribute metaAttribute;
    private DataOperationType dataOperationType;
    private Map<String, EavData> values = new HashMap<>();

    public EavDataEntity() {
        /*An empty constructor*/
    }

    //region Getters and Setters

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public DataOperationType getOperation() {
        return dataOperationType;
    }

    public void setOperation(DataOperationType type) {
        dataOperationType = type;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    //endregion

    public void put(final String attribute, EavData data) {
        MetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
        MetaType type = metaAttribute.getMetaType();

        if (type == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E25,attribute, metaClass.getClassName()));

        if (data == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E26));

        Class<?> valueClass = data.getClass();
        Class<?> expValueClass;

        if (type.isComplex())
            if (type.isSet())
                expValueClass = EavDataSet.class;
            else
                expValueClass = EavDataEntity.class;
        else {
            if (type.isSet()) {
                expValueClass = EavDataSet.class;
                valueClass = data.getClass();
            } else {
                MetaValue metaValue = (MetaValue) type;
                valueClass = ((EavDataSimple)data).getValue().getClass();
                expValueClass = metaValue.getDataType().getDataTypeClass();
            }
        }

        if (expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
            throw new IllegalArgumentException(Errors.compose(Errors.E27, metaClass.getClassName(),expValueClass,valueClass));

        data.setDataContainer(this);
        data.setMetaAttribute(metaAttribute);

        values.put(attribute, data);
    }

    public EavDataSimple getDataValue(String attribute) {
        //TODO:
        return null;
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
        result += 31 * result + metaClass.hashCode();
        result += 31 * result + values.hashCode();

        return result;
    }

    @Override
    public EavDataEntity clone() {
        //TODO:
        return null;
    }

    @Override
    public MetaType getMetaType() {
        return metaClass;
    }

    @Override
    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    @Override
    public EavData getDataContainer() {
        return dataContainer;
    }

    @Override
    public void setMetaType(MetaType metaType) {
        this.metaClass = (MetaClass) metaType;
    }

    @Override
    public void setMetaAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    @Override
    public void setDataContainer(EavData dataContainer) {
        this.dataContainer = dataContainer;
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


