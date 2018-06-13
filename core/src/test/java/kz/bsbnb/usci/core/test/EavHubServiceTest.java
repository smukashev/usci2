package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.factory.EavDataFactory;
import kz.bsbnb.usci.core.service.BaseEntityProcessor;
import kz.bsbnb.usci.core.service.EavHubService;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import kz.bsbnb.usci.model.eav.base.BaseValue;
import kz.bsbnb.usci.model.eav.base.OperType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EavHubServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(EavHubServiceTest.class);

    @Autowired
    private EavDataFactory eavDataFactory;
    @Autowired
    private EavHubService eavHubService;

    @Before
    public void setUp() {
        //
    }

    @Test
    public void primaryContractKeyStringTest() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));

        String key = eavHubService.getKeyString(primaryContract);

        logger.info("eav hub key: " + key);

        Assert.assertEquals("AICC.1354927\\|~~~\\|31.08.2017", key);

        eavHubService.insert(primaryContract);

        //Long entityId = eavHubService.find(primaryContract);
    }

}