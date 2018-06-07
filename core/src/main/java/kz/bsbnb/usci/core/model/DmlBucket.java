package kz.bsbnb.usci.core.model;

import kz.bsbnb.usci.model.eav.base.BaseType;

public interface DmlBucket {

    void registerAsInserted(BaseType persistableObject);

    void registerAsUpdated(BaseType persistableObject);

    void registerAsDeleted(BaseType persistableObject);

}
