package kz.bsbnb.usci.model.eav.base;

import java.util.*;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;
import kz.bsbnb.usci.model.eav.meta.MetaValue;

/**
 * @author BSB
 */

public class BaseSet implements BaseContainer {
    private UUID uuid = UUID.randomUUID();

    private BaseContainer baseContainer;
    private MetaType metaType;
    private Set<BaseValue> values = new HashSet<>();

    public BaseSet() {
        /*An empty constructor*/
    }

    public BaseSet(MetaType metaType) {
        this.metaType = metaType;
    }

    @Override
    public MetaType getMetaType() {
        return metaType;
    }

    public void put(BaseValue value) {
        value.setBaseContainer(this);
        values.add(value);
    }

    public void remove(BaseValue value) {
        values.remove(value);
    }

    @Override
    public Set<BaseValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (BaseValue value : values) {
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

    public Object getElSimple(String filter) {
        if (metaType.isComplex() || metaType.isSet()) {
            throw new IllegalArgumentException(Errors.compose(Errors.E35));
        }

        for (BaseValue value : values) {
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

        for (BaseValue value : values) {
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
