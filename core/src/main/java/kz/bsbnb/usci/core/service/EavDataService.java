package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.data.DataEntity;

public interface EavDataService {

    void process(DataEntity saving);

    DataEntity load(long entityId);

}
