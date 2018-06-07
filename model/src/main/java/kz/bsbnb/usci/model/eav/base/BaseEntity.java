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
        MetaType metaType = metaAttribute.getMetaType();

        if (metaType == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E25,attribute, metaClass.getClassName()));

        if (baseValue == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E26));

        Class<?> valueClass = baseValue.getValue().getClass();
        Class<?> expValueClass;

        if (baseValue.getValue() != null) {
            if (metaType.isComplex())
                if (metaType.isSet())
                    expValueClass = BaseSet.class;
                else
                    expValueClass = BaseEntity.class;
            else {
                if (metaType.isSet()) {
                    expValueClass = BaseSet.class;
                    valueClass = baseValue.getClass();
                } else {
                    MetaValue metaValue = (MetaValue) metaType;
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

    public Object getEl(String path) {
        if (path.equals("ROOT"))
            return getId();

        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        BaseEntity entity = this;
        MetaClass theMeta = metaClass;
        Object valueOut = null;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String arrayIndexes = null;

            if (token.contains("[")) {
                arrayIndexes = token.substring(token.indexOf("[") + 1, token.length() - 1);
                token = token.substring(0, token.indexOf("["));
            }

            MetaAttribute attribute = theMeta.getMetaAttribute(token);

            MetaType type = null;
            try {
                type = attribute.getMetaType();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (entity == null)
                return null;

            BaseValue value = entity.getBaseValue(token);

            if (value == null || value.getValue() == null) {
                valueOut = null;
                break;
            }

            valueOut = value.getValue();

            if (type == null)
                throw new IllegalStateException(Errors.compose(Errors.E46));

            if (type.isSet()) {
                if (arrayIndexes != null) {
                    valueOut = ((BaseSet) valueOut).getEl(arrayIndexes.replaceAll("->", "."));
                    type = ((MetaSet) type).getMetaType();
                } else {
                    return valueOut;
                }
            }

            if (type.isComplex()) {
                entity = (BaseEntity) valueOut;
                theMeta = (MetaClass) type;
            } else {
                if (tokenizer.hasMoreTokens()) {
                    throw new IllegalArgumentException(Errors.compose(Errors.E13));
                }
            }
        }

        return valueOut;
    }

    boolean equalsToString(HashMap<String, String> params) {
        for (String fieldName : params.keySet()) {
            String ownFieldName;
            String innerPath = null;
            if (fieldName.contains(".")) {
                ownFieldName = fieldName.substring(0, fieldName.indexOf("."));
                innerPath = fieldName.substring(fieldName.indexOf(".") + 1);
            } else {
                ownFieldName = fieldName;
            }

            MetaType mtype = metaClass.getAttributeType(ownFieldName);

            if (mtype == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E9,fieldName));

            if (mtype.isSet())
                throw new IllegalArgumentException(Errors.compose(Errors.E10,fieldName));

            BaseValue baseValue = getBaseValue(ownFieldName);

            if (mtype.isComplex()) {
                baseValue = ((BaseEntity) (baseValue.getValue())).getBaseValue(innerPath);
                mtype = ((MetaClass) mtype).getAttributeType(innerPath);
            }

            if (!baseValue.equalsToString(params.get(fieldName), ((MetaValue) mtype).getMetaDataType()))
                return false;
        }

        return true;
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

    @Override
    public int getValueCount() {
        return values.size();
    }

}


