package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.data.BaseEntity;

/**
 * @author BSB
 */

public interface EavDataService {

    void process(BaseEntity saving);

    BaseEntity load(long entityId);

}