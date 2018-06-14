package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

public interface BaseEntityApplyService {

    BaseEntity apply(long respondentId, final BaseEntity baseEntitySaving, BaseEntity baseEntityLoaded, BaseEntityManager baseEntityManager);

    void applyToDb(BaseEntityManager baseEntityManager);

    void applyToSchemaEavXml(final BaseEntity baseEntity);

}
