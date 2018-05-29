package kz.bsbnb.usci.core;

import kz.bsbnb.usci.model.eav.OperationPool;
import kz.bsbnb.usci.model.eav.data.DataEntity;

public interface DataManager {

    void process(DataEntity saving, DataEntity applied, OperationPool pool);

    DataEntity load(long entityId);

}
