package kz.bsbnb.usci.model.eav.base;

import java.util.Collection;

public interface BaseContainer extends BaseType {

    BaseContainer getBaseContainer();

    void setBaseContainer(BaseContainer baseContainer);

    Collection<BaseValue> getValues();

}
