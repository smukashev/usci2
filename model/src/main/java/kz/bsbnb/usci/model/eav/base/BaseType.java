package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.eav.meta.MetaType;

/**
 * @author Artur Tkachenko
 * @author Alexandr Motov
 * @author Kanat Tulbassiev
 * @author Baurzhan Makhambetov
 * @author Jandos Iskakov
 * @author Emil Essanov
 * @author Olzhas Kaliaskar
 */

public interface BaseType {

    MetaType getMetaType();

    boolean isSet();

    boolean isComplex();

}



