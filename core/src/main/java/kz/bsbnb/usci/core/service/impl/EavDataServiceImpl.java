package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.EavDataService;
import kz.bsbnb.usci.model.eav.data.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author BSB
 */

@Service
public class EavDataServiceImpl implements EavDataService {
    private static final Logger logger = LoggerFactory.getLogger(EavDataService.class);

    private final NamedParameterJdbcTemplate npJdbcTemplate;

    public EavDataServiceImpl(NamedParameterJdbcTemplate npJdbcTemplate) {
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Override
    //TODO: желательно чтобы эта переменная была final
    public void process(BaseEntity saving) {
        logger.info("eav_data: processing entity");


    }

    @Override
    public BaseEntity load(long entityId) {
        //TODO:
        return null;
    }



}
