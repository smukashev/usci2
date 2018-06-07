package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.eav.meta.MetaType;

/**
 * @author BSB
 */

public interface BaseType {

    MetaType getMetaType();

    boolean isSet();

    boolean isComplex();

}



