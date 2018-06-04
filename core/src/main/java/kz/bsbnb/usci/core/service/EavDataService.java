package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.data.EavDataEntity;

public interface EavDataService {

    void process(EavDataEntity saving);

    EavDataEntity load(long entityId);

}
