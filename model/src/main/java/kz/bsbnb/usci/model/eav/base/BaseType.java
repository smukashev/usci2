package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public interface BaseType {

    //void setMetaType(MetaType metaType);

    MetaType getMetaType();

    /*BaseType getBaseContainer();

    void setBaseContainer(BaseType baseContainer);*/

    boolean isSet();

    boolean isComplex();

}



