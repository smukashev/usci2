package kz.bsbnb.usci.model.eav.base;

import java.util.Collection;

/**
 * @author BSB
 */

public interface BaseContainer extends BaseType {

    int getValueCount();

    Collection<BaseValue> getValues();

}
