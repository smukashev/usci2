package kz.bsbnb.usci.model.eav.base;

import java.util.*;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;
import kz.bsbnb.usci.model.eav.meta.MetaValue;

/**
 * @author BSB
 */

public class BaseSet implements BaseContainer, Cloneable {
    private UUID uuid = UUID.randomUUID();
    private MetaType metaType;

    // использование HashMap вместе HashSet сделано преднамеренно
    // необходимо допускать дублирование записей в коллекцию во время парсинга,
    // и затем выявлять дубликаты через бизнес правила
    // иначе бы HashSet перетирал дубликаты и в коллекций оставались бы только уникальные записи
    // в качестве ключей HashMap служат UUID которые генерируют уникальные значения
    // обычные массивы тоже можно было использовать, но ключи HashMap можно зайдествовать в коде
    private Map<String, BaseValue> values = new HashMap<>();

    public BaseSet() {
        /*Пустой конструктор*/
    }

    public BaseSet(MetaType metaType) {
        this.metaType = metaType;
    }

    @Override
    public MetaType getMetaType() {
        return metaType;
    }

    public void put(BaseValue baseValue) {
        if (baseValue == null)
            throw new IllegalArgumentException("Добавлять в сет пустое значение не допустимо");

        baseValue.setBaseContainer(this);

        UUID uuid = UUID.randomUUID();
        put(uuid.toString(), baseValue);
    }

    private void put(String name, BaseValue baseValue) {
        if (baseValue == null || baseValue.getValue() == null)
            throw new IllegalArgumentException("Добавлять в множество пустое значение не допустимо");

        if (name == null) {
            UUID uuid = UUID.randomUUID();
            put(uuid.toString(), baseValue);
        }

        if (baseValue.getValue() instanceof BaseSet)
            throw new UnsupportedOperationException("Не правильное значение множества");

        baseValue.setBaseContainer(this);

        values.put(name, baseValue);
    }

    public void remove(BaseValue value) {
        values.remove(value);
    }

    @Override
    public Collection<BaseValue> getValues() {
        return values.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (BaseValue value : values.values()) {
            if (first) {
                sb.append(value.getValue().toString());
                first = false;
            } else {
                sb.append(", ").append(value.getValue().toString());
            }
        }

        sb.append("]");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public BaseSet clone() {
        BaseSet baseSetCloned;
        try {
            baseSetCloned = (BaseSet) super.clone();

            HashMap<String, BaseValue> valuesCloned = new HashMap<>();

            for (String attribute : values.keySet()) {
                BaseValue baseValue = values.get(attribute);
                BaseValue baseValueCloned = baseValue.clone();
                baseValueCloned.setBaseContainer(baseSetCloned);
                valuesCloned.put(attribute, baseValueCloned);
            }

            baseSetCloned.values = valuesCloned;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(Errors.compose(Errors.E31));
        }

        return baseSetCloned;
    }

    @Override
    public boolean isSet() {
        return true;
    }

    @Override
    public boolean isComplex() {        
        return metaType.isComplex();
    }

    public Object getElSimple(String filter) {
        if (metaType.isComplex() || metaType.isSet()) {
            throw new IllegalArgumentException(Errors.compose(Errors.E35));
        }

        for (BaseValue value : values.values()) {
            Object innerValue = value.getValue();
            if (innerValue == null)
                continue;

            if (value.equalsToString(filter, ((MetaValue) metaType).getMetaDataType()))
                return innerValue;
        }

        return null;
    }

    public Object getElComplex(String filter) {
        if (!metaType.isComplex() || metaType.isSet())
            throw new IllegalArgumentException(Errors.compose(Errors.E33));

        StringTokenizer tokenizer = new StringTokenizer(filter, ",");

        Object valueOut = null;
        HashMap<String, String> params = new HashMap<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            StringTokenizer innerTokenizer = new StringTokenizer(token, "=");

            String fieldName = innerTokenizer.nextToken().trim();
            if (!innerTokenizer.hasMoreTokens())
                throw new IllegalStateException(Errors.compose(Errors.E34));

            String fieldValue = innerTokenizer.nextToken().trim();

            params.put(fieldName, fieldValue);
        }

        for (BaseValue value : values.values()) {
            Object innerValue = value.getValue();
            if (innerValue == null)
                continue;

            if (((BaseEntity) innerValue).equalsToString(params))
                return innerValue;
        }

        return valueOut;
    }

    public Object getEl(String filter) {
        if (metaType.isComplex())
            return getElComplex(filter);
        return getElSimple(filter);
    }

    @Override
    public int getValueCount() {
        return values.size();
    }

}
