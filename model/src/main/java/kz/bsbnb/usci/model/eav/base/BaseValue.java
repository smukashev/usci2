package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaDataType;
import kz.bsbnb.usci.model.eav.meta.MetaSet;
import kz.bsbnb.usci.model.eav.meta.MetaType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author BSB
 */

public class BaseValue implements Cloneable {
    private UUID uuid = UUID.randomUUID();
    private BaseContainer baseContainer;
    private MetaAttribute metaAttribute;
    private Object newValue = null;
    private Object value;
    private Boolean changed = Boolean.FALSE;
    private Boolean mock = Boolean.FALSE;

    public BaseValue() {
        /*Пустой конструктор*/
    }

    public BaseValue(Object value) {
        this.value = value;
    }

    public BaseContainer getBaseContainer() {
        return baseContainer;
    }

    public void setBaseContainer(BaseContainer baseContainer) {
        this.baseContainer = baseContainer;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setNewValue(Object baseSimple) {
        this.newValue = baseSimple;
    }

    public Object getNewValue() {
        return newValue;
    }

    public MetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public void setMetaAttribute(MetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Boolean isChanged() {
        return changed;
    }

    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    public Boolean getMock() {
        return mock;
    }

    public void setMock(Boolean mock) {
        this.mock = mock;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || (getClass() != obj.getClass()))
            return false;

        BaseValue that = (BaseValue) obj;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    public boolean equalsToString(String str, MetaDataType type) {
        switch (type) {
            case INTEGER:
                if (value.equals(Integer.parseInt(str)))
                    return true;
                break;
            case DATE:
                throw new UnsupportedOperationException(Errors.compose(Errors.E41));
            case STRING:
                if (value.equals(str))
                    return true;
                break;
            case BOOLEAN:
                if (value.equals(Boolean.parseBoolean(str)))
                    return true;
                break;
            case DOUBLE:
                if (value.equals(Double.parseDouble(str)))
                    return true;
                break;
            default:
                throw new IllegalStateException(Errors.compose(Errors.E7, type));
        }

        return false;
    }

    public boolean equalsByValue(BaseValue baseValue) {
        MetaAttribute thisMetaAttribute = this.getMetaAttribute();
        MetaAttribute thatMetaAttribute = baseValue.getMetaAttribute();

        if (thisMetaAttribute == null || thatMetaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E38));

        return thisMetaAttribute.getId().equals(thatMetaAttribute.getId()) &&
                this.equalsByValue(thisMetaAttribute.getMetaType(), baseValue);
    }

    public boolean equalsByValue(MetaType metaType, BaseValue baseValue) {
        Object thisValue = this.getValue();
        Object thatValue = baseValue.getValue();

        if (thisValue == null || thatValue == null)
            throw new RuntimeException(Errors.compose(Errors.E39));

        if (metaType.isComplex()) {
            if (metaType.isSet()) {
                BaseSet thisBaseSet = (BaseSet) thisValue;
                BaseSet thatBaseSet = (BaseSet) thatValue;

                List<Long> thisIds = thisBaseSet.getValues().stream()
                        .map(childBaseValue -> ((BaseEntity) childBaseValue.getValue()).getId())
                        .collect(Collectors.toList());

                List<Long> thatIds = thatBaseSet.getValues().stream()
                        .map(childBaseValue -> ((BaseEntity) childBaseValue.getValue()).getId())
                        .collect(Collectors.toList());

                Collections.sort(thisIds);
                Collections.sort(thatIds);

                return thisIds.equals(thatIds);
            } else {
                BaseEntity thisBaseEntity = (BaseEntity) thisValue;
                BaseEntity thatBaseEntity = (BaseEntity) thatValue;
                return thisBaseEntity.getId().equals(thatBaseEntity.getId()) && thisBaseEntity.getId() != null;
            }
        } else {
            if (metaType.isSet()) {
                MetaSet metaSet = (MetaSet) metaType;
                MetaType childMetaType = metaSet.getMetaType();

                BaseSet thisBaseSet = (BaseSet) thisValue;
                BaseSet thatBaseSet = (BaseSet) thatValue;

                boolean baseValueNotFound;
                Set<UUID> processedUuids = new HashSet<>();
                for (BaseValue thisBaseValue : thisBaseSet.getValues()) {
                    baseValueNotFound = true;
                    for (BaseValue thatBaseValue : thatBaseSet.getValues()) {
                        if (processedUuids.contains(thatBaseValue.getUuid()))
                            continue;

                        if (thisBaseValue.equalsByValue(childMetaType, thatBaseValue)) {
                            processedUuids.add(thatBaseValue.getUuid());
                            baseValueNotFound = false;
                            break;
                        }
                    }

                    if (baseValueNotFound)
                        return false;
                }

                return true;
            } else {
                return thisValue.equals(thatValue);
            }
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public BaseValue clone() {
        BaseValue baseValue;
        try {
            baseValue = (BaseValue) super.clone();

            if (value != null) {
                if (value instanceof BaseEntity)
                    baseValue.setValue(((BaseEntity) value).clone());
                if (value instanceof BaseSet)
                    baseValue.setValue(((BaseSet) value).clone());
            }
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(Errors.compose(Errors.E37));
        }
        return baseValue;
    }

    @Override
    public String toString() {
        if (value == null)
            return null;

        return value.toString();
    }

}


