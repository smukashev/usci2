package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.core.model.EavHub;
import kz.bsbnb.usci.model.eav.base.BaseEntity;

public interface EavHubService {

    Long insert(EavHub eavHub);

    Long find(BaseEntity baseEntity);

    String getKeyString(BaseEntity baseEntity);

    EavHub find(Long entityId);

    void delete(Long entityId);

    void update(EavHub eavHub);

}
