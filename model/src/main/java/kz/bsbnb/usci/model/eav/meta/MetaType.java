package kz.bsbnb.usci.model.eav.meta;

import java.io.Serializable;

/**
 * @author BSB
 */

public interface MetaType extends Serializable {

    String toString(String prefix);

    boolean isSet();

    boolean isComplex();

}

