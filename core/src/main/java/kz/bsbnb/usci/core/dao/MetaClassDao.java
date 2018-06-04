package kz.bsbnb.usci.core.dao;

import kz.bsbnb.usci.model.eav.meta.MetaClass;

import java.util.List;

public interface MetaClassDao {

    MetaClass load(String className);

    MetaClass load(Long id);

    List<MetaClass> loadAll();

}
