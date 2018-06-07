package kz.bsbnb.usci.model.eav.base;

import java.time.LocalDate;
import java.util.*;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.Persistable;
import kz.bsbnb.usci.model.eav.meta.*;

/**
 * @author BSB
 */

public class BaseEntity extends Persistable implements BaseContainer {
    private UUID uuid = UUID.randomUUID();
    private MetaClass metaClass;
    private LocalDate reportDate;
    private BaseContainer baseContainer;
    private Long respondentId;
    private Long batchId;
    private OperType operType;
    private Map<String, BaseValue> values = new HashMap<>();

    public BaseEntity() {
        /*An empty constructor*/
    }

    public BaseEntity(MetaClass metaClass, LocalDate reportDate, Long respondentId, Long batchId) {
        this.metaClass = metaClass;
        this.reportDate = reportDate;
        this.respondentId = respondentId;
        this.batchId = batchId;
    }

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

    public UUID getUuid() {
        return uuid;
    }

    public void put(final String attribute, BaseValue baseValue) {
        MetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
        MetaType type = metaAttribute.getMetaType();

        if (type == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E25,attribute, metaClass.getClassName()));

        if (baseValue == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E26));

        Class<?> valueClass = baseValue.getValue().getClass();
        Class<?> expValueClass;

        if (baseValue.getValue() != null) {
            if (type.isComplex())
                if (type.isSet())
                    expValueClass = BaseSet.class;
                else
                    expValueClass = BaseEntity.class;
            else {
                if (type.isSet()) {
                    expValueClass = BaseSet.class;
                    valueClass = baseValue.getClass();
                } else {
                    MetaValue metaValue = (MetaValue) type;
                    expValueClass = metaValue.getMetaDataType().getDataTypeClass();
                }
            }

            if (expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
                throw new IllegalArgumentException(Errors.compose(Errors.E27, metaClass.getClassName(), expValueClass, valueClass));
        }

        baseValue.setBaseContainer(this);
        baseValue.setMetaAttribute(metaAttribute);

        values.put(attribute, baseValue);
    }

    public BaseValue getBaseValue(String attribute) {
        if (attribute.contains(".")) {
            int index = attribute.indexOf(".");
            String parentAttribute = attribute.substring(0, index);
            String childAttribute = attribute.substring(index, attribute.length() - 1);

            MetaType metaType = metaClass.getMetaAttribute(parentAttribute).getMetaType();
            if (metaType == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E12, metaClass.getClassName(),parentAttribute));

            if (metaType.isComplex() && !metaType.isSet()) {
                BaseValue baseValue = values.get(parentAttribute);
                if (baseValue == null)
                    return null;

                BaseEntity baseEntity = (BaseEntity) baseValue.getValue();
                if (baseEntity == null)
                    return null;
                else
                    return baseEntity.getBaseValue(childAttribute);
            } else {
                return null;
            }
        } else {
            MetaType metaType = metaClass.getMetaAttribute(attribute).getMetaType();

            if (metaType == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E12, metaClass.getClassName(),attribute));

            return values.get(attribute);
        }
    }

    @Override
    public Collection<BaseValue> getValues() {
        return values.values();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public String toString() {
        return BaseEntityOutput.toString(this);
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public MetaType getMetaType() {
        return metaClass;
    }

    public MetaAttribute getMetaAttribute(String attribute) {
        return metaClass.getMetaAttribute(attribute);
    }

    @Override
    public BaseContainer getBaseContainer() {
        return baseContainer;
    }

    @Override
    public void setBaseContainer(BaseContainer baseContainer) {
        this.baseContainer = baseContainer;
    }

    public Long getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Set<String> getAttributes() {
        return values.keySet();
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


