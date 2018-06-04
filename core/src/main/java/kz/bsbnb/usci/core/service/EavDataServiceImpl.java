package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.data.EavDataEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author BSB
 */

@Service
public class EavDataServiceImpl implements EavDataService {
    private static final Logger logger = LoggerFactory.getLogger(EavDataService.class);

    @Override
    public void process(EavDataEntity saving) {
        logger.info("eav_data: processing entity");

        //TODO:
    }

    @Override
    public EavDataEntity load(long entityId) {
        //TODO:

        return null;
    }

    private void insertEavDataEntity(EavDataEntity eavDataEntity) {

    }



}
