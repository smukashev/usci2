package kz.bsbnb.usci.core.repository;

import kz.bsbnb.usci.model.eav.meta.MetaClass;

import java.util.List;

/**
 * @author Baurzhan Makhambetov
 */

public interface MetaClassRepository {
    MetaClass getMetaClass(String className);

    MetaClass getMetaClass(long id);

    List<MetaClass> getMetaClasses();

    void resetCache();
}
