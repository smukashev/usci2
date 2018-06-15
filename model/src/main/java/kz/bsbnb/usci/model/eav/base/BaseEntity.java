package kz.bsbnb.usci.model.eav.base;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.Persistable;
import kz.bsbnb.usci.model.eav.meta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BSB
 */

public class BaseEntity extends Persistable implements BaseContainer, Cloneable {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    private UUID uuid = UUID.randomUUID();
    private MetaClass metaClass;
    private LocalDate reportDate;
    private Long respondentId;
    private Long batchId;
    private OperType operType;
    private Map<String, BaseValue> values = new HashMap<>();
    private Long userId;
    private Parent parent;

    public BaseEntity() {
        /*An empty constructor*/
    }

    public BaseEntity(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public BaseEntity(long id, MetaClass metaClass, Long respondentId) {
        super(id);
        this.metaClass = metaClass;
        this.respondentId = respondentId;
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

    public void setParent(BaseEntity parentEntity, MetaAttribute metaAttribute) {
        parent = new Parent(parentEntity, metaAttribute);
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

    // метод предназначен для сверки двух сущностей по ключевым полям (все значения ключей должны совпадать)
    // сверка сетов в качестве ключей не предусмотрена
    // сущности должны быть одного мета класса и респондента
    public boolean equalsByKey(BaseEntity baseEntity) {
        if (this == baseEntity)
            return true;

        if (baseEntity == null || getClass() != baseEntity.getClass())
            return false;

        if (!this.getMetaClass().equals(baseEntity.getMetaClass()))
            return false;

        if (!this.respondentId.equals(baseEntity.getRespondentId()))
            return false;

        for (String name : this.metaClass.getAttributeNames()) {
            MetaAttribute metaAttribute = this.metaClass.getMetaAttribute(name);
            MetaType metaType = metaAttribute.getMetaType();

            if (metaAttribute.isKey() && metaType.isSet())
                continue;

            if (metaAttribute.isKey()) {
                BaseValue thisBaseValue = this.getBaseValue(name);
                BaseValue thatBaseValue = baseEntity.getBaseValue(name);

                if (metaType.isComplex()) {
                    if (!((BaseEntity) thisBaseValue.getValue()).equalsByKey((BaseEntity) thatBaseValue.getValue()))
                        return false;
                } else {
                    try {
                        if (!thisBaseValue.getValue().equals(thatBaseValue.getValue()))
                            return false;
                    } catch (NullPointerException ex) {
                        logger.debug("NullPointerException baseEntityId=" + baseEntity.getId() + " , batchId=" + baseEntity.getBatchId() + ", attributeName=" + name);
                        return false;
                    }
                }
            }

            if (metaAttribute.isNullableKey()) {
                if (!metaType.isComplex()) {
                    BaseValue thisBaseValue = this.getBaseValue(name);
                    BaseValue thatBaseValue = baseEntity.getBaseValue(name);

                    if (thisBaseValue == null && thatBaseValue != null) return false;
                    if (thisBaseValue != null && thatBaseValue == null) return false;

                    if (thisBaseValue != null && thatBaseValue != null) {
                        if (thisBaseValue.getValue() == null && thatBaseValue.getValue() != null) return false;
                        if (thisBaseValue.getValue() != null && thatBaseValue.getValue() == null) return false;

                        if (thisBaseValue.getValue() != null && thatBaseValue.getValue() != null)
                            if (!thisBaseValue.getValue().equals(thatBaseValue.getValue()))
                                return false;
                    }
                } else
                    throw new IllegalStateException("isComplex isNullableKey not supported");
            }
        }

        if (this.metaClass.parentIsKey()) {
            //TODO: добавить проверку parentIsKey

        }

        return true;
    }

    @Override
    public Collection<BaseValue> getValues() {
        return values.values();
    }

    // проверяет на соответсвие атрибутов
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        BaseEntity that = (BaseEntity) obj;

        if (!this.getMetaClass().getId().equals(that.getMetaClass().getId()))
            return false;

        int thisValueCount = this.getValueCount();
        int thatValueCount = that.getValueCount();

        if (thisValueCount != thatValueCount)
            return false;

        for (String attributeName : metaClass.getAttributeNames()) {
            MetaAttribute metaAttribute = metaClass.getMetaAttribute(attributeName);
            MetaType metaType = metaAttribute.getMetaType();

            BaseValue thisBaseValue = this.getBaseValue(attributeName);
            BaseValue thatBaseValue = that.getBaseValue(attributeName);

            if (thisBaseValue == null && thatBaseValue == null)
                continue;

            if (thisBaseValue == null || thatBaseValue == null)
                return false;

            if (metaType.isSet()) {
                BaseSet thisBaseSet = (BaseSet) thisBaseValue.getValue();
                BaseSet thatBaseSet = (BaseSet) thatBaseValue.getValue();

                if (thisBaseSet == null && thatBaseSet == null)
                    continue;

                if (thisBaseSet == null || thatBaseSet == null)
                    return false;

                if (thisBaseSet.getValueCount() != thatBaseSet.getValueCount())
                    return false;

                for (BaseValue thisChildBaseValue : thisBaseSet.getValues()) {
                    Object thisChildValue = thisChildBaseValue.getValue();

                    boolean childValueFound = false;

                    for (BaseValue thatChildBaseValue : thatBaseSet.getValues()) {
                        Object thatChildValue = thatChildBaseValue.getValue();

                        if (metaType.isComplex() && metaAttribute.isImmutable()) {
                            if (!((BaseEntity) thisChildValue).getId().equals(((BaseEntity) thatChildValue).getId()))
                                return false;

                            continue;
                        }

                        if (thisChildValue.equals(thatChildValue))
                            childValueFound = true;
                    }

                    if (!childValueFound)
                        return false;
                }
            } else {
                Object thisValue = thisBaseValue.getValue();
                Object thatValue = thatBaseValue.getValue();

                if (thisValue == null && thatValue == null)
                    continue;

                if (thisValue == null || thatValue == null)
                    return false;

                if (metaType.isComplex() && metaAttribute.isImmutable()) {
                    if (!((BaseEntity) thisValue).getId().equals(((BaseEntity) thatValue).getId()))
                        return false;

                    continue;
                }

                if (!thisValue.equals(thatValue))
                    return false;

                // Проверка на изменение ключевых полей
                if (!metaType.isComplex() && (thisBaseValue.getNewValue() != null || thatBaseValue.getNewValue() != null))
                    return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return BaseEntityOutput.toString(this);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += 31 * result + metaClass.hashCode();
        result += 31 * result + (int) (respondentId ^ (respondentId >>> 32));//TODO:
        result += 31 * result + values.hashCode();

        return result;
    }

    @Override
    public BaseEntity clone() {
        BaseEntity baseEntityCloned;

        try {
            baseEntityCloned = (BaseEntity) super.clone();

            Map<String, BaseValue> valuesCloned = new HashMap<>();

            for (String attribute : values.keySet()) {
                BaseValue baseValue = values.get(attribute);
                BaseValue baseValueCloned = baseValue.clone();

                baseValueCloned.setMetaAttribute(getMetaAttribute(attribute));
                baseValueCloned.setBaseContainer(baseEntityCloned);
                valuesCloned.put(attribute, baseValueCloned);
            }

            baseEntityCloned.values = valuesCloned;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(Errors.compose(Errors.E8));
        }

        return baseEntityCloned;
    }

    @Override
    public MetaClass getMetaType() {
        return metaClass;
    }

    public MetaType getAttributeType(String attribute) {
        if (attribute.contains(".")) {
            int index = attribute.indexOf(".");
            String parentIdentifier = attribute.substring(0, index);

            MetaType metaType = metaClass.getAttributeType(parentIdentifier);
            if (metaType.isComplex() && !metaType.isSet()) {
                MetaClass childMeta = (MetaClass) metaType;
                String childIdentifier = attribute.substring(index, attribute.length() - 1);
                return childMeta.getAttributeType(childIdentifier);
            } else {
                return null;
            }
        } else {
            return metaClass.getAttributeType(attribute);
        }
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

    public Set<String> getAttributeNames() {
        return values.keySet();
    }

    public Stream<MetaAttribute> getAttributes() {
        return values.keySet().stream().map(metaClass::getMetaAttribute);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
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

    private class Parent implements Serializable {
        private BaseEntity entity;
        private MetaAttribute attribute;

        public Parent(BaseEntity entity, MetaAttribute attribute) {
            this.entity = entity;
            this.attribute = attribute;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Parent that = (Parent) o;

            if (entity == null || attribute == null || that.entity == null || that.attribute == null)
                throw new IllegalStateException(o.toString());

            if (!attribute.getId().equals(that.attribute.getId()))
                return false;

            if (entity.getId() != null && that.entity.getId() != null && entity.getId().equals(that.entity.getId()))
                return true;
            if (entity.getId() != null || that.entity.getId() != null)
                return false;
            if (entity.getId() == null && that.entity.getId() == null && entity.equalsByKey(that.entity))
                return true;

            return false;
        }
    }

}


