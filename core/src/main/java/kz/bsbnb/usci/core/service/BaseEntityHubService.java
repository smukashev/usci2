package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.core.model.EavHub;
import kz.bsbnb.usci.model.eav.base.BaseEntity;

public interface BaseEntityHubService {

    void insert(EavHub eavHub);

    void insert(BaseEntity baseEntity);

    Long find(BaseEntity baseEntity);

    String getKeyString(BaseEntity baseEntity);

    EavHub find(Long entityId);

    void delete(Long entityId);

    void update(EavHub eavHub);

}
