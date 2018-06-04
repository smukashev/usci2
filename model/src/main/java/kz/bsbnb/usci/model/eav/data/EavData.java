package kz.bsbnb.usci.model.eav.data;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;

public interface EavData {

    void setMetaType(MetaType metaType);

    void setMetaAttribute(MetaAttribute metaAttribute);

    MetaType getMetaType();

    MetaAttribute getMetaAttribute();

    EavData getDataContainer();

    void setDataContainer(EavData dataContainer);

    boolean isSet();

    boolean isComplex();

}



