package kz.bsbnb.usci.core.dao;

import kz.bsbnb.usci.core.model.EavHub;

/**
 * @author BSB
 */

public interface EavHubDao {

    Long insert(EavHub eavHub);

    Long find(Long respondentId, Long metaClassId, String entityKey, Long parentEntityId);

    EavHub find(Long entityId);

    void delete(Long entityId);

    void update(EavHub eavHub);

}
