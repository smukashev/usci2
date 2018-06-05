package kz.bsbnb.usci.model.eav.data;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public interface BaseType {

    void setMetaType(MetaType metaType);

    void setMetaAttribute(MetaAttribute metaAttribute);

    MetaType getMetaType();

    MetaAttribute getMetaAttribute();

    BaseType getBaseContainer();

    void setBaseContainer(BaseType baseContainer);

    boolean isSet();

    boolean isComplex();

}



