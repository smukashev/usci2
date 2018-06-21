package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.factory.EavBaseFactory;
import kz.bsbnb.usci.core.service.BaseEntityHubService;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

/**
 * @author Jandos Iskakov
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseEntityHubServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityHubServiceTest.class);

    @Autowired
    private EavBaseFactory eavBaseFactory;
    @Autowired
    private BaseEntityHubService baseEntityHubService;

    @Before
    public void setUp() {
        //
    }

    @Test
    public void primaryContractKeyStringTest() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity primaryContract = eavBaseFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));

        String key = baseEntityHubService.getKeyString(primaryContract);

        logger.info("eav hub key: " + key);

        Assert.assertEquals("AICC.1354927\\|~~~\\|31.08.2017", key);

        baseEntityHubService.insert(primaryContract);

        //Long entityId = baseEntityHubService.find(primaryContract);
    }

}