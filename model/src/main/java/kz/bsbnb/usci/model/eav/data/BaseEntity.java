package kz.bsbnb.usci.model.eav.data;

import java.time.LocalDate;
import java.util.*;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.Persistable;
import kz.bsbnb.usci.model.eav.meta.*;

/**
 * @author BSB
 */

public class BaseEntity extends Persistable implements BaseType {
    private MetaClass metaClass;
    private LocalDate reportDate;
    private BaseType baseContainer;
    private Long respondentId;
    private MetaAttribute metaAttribute;
    private OperType operType;
    private Map<String, BaseType> values = new HashMap<>();

    public BaseEntity() {
        /*An empty constructor*/
    }

    //region Getters and Setters

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public OperType getOperation() {
        return operType;
    }

    public void setOperation(OperType type) {
        operType = type;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    //endregion

    public void put(final String attribute, BaseType data) {
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
                expValueClass = BaseSet.class;
            else
                expValueClass = BaseEntity.class;
        else {
            if (type.isSet()) {
                expValueClass = BaseSet.class;
                valueClass = data.getClass();
            } else {
                MetaValue metaValue = (MetaValue) type;
                valueClass = ((BaseSimple)data).getValue().getClass();
                expValueClass = metaValue.getMetaDataType().getDataTypeClass();
            }
        }

        if (expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
            throw new IllegalArgumentException(Errors.compose(Errors.E27, metaClass.getClassName(),expValueClass,valueClass));

        data.setBaseContainer(this);
        data.setMetaAttribute(metaAttribute);

        values.put(attribute, data);
    }

    public Map<String, BaseType> getValues() {
        return values;
    }

    public BaseSimple getDataValue(String attribute) {
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
    public BaseEntity clone() {
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
    public BaseType getBaseContainer() {
        return baseContainer;
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
    public void setBaseContainer(BaseType baseContainer) {
        this.baseContainer = baseContainer;
    }

    public Long getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return true;
    }
    
}


