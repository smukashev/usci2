package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

public interface BaseEntityStoreService {

    BaseEntity processBaseEntity(final BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager);

    void storeBaseManager(BaseEntityManager baseEntityManager);

    void storeBaseEntityToSchemaEavXml(final BaseEntity baseEntity);

}
