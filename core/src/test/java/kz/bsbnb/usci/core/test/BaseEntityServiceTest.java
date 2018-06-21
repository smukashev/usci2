package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.factory.EavBaseFactory;
import kz.bsbnb.usci.core.service.BaseEntityService;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
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
public class BaseEntityServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityServiceTest.class);

    @Autowired
    private EavBaseFactory eavBaseFactory;
    @Autowired
    private BaseEntityService baseEntityService;

    @Before
    public void setUp() {
        //
    }

    @Test
    public void creditExistsTest() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity credit = eavBaseFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);

        Assert.assertTrue(baseEntityService.existsBaseEntity(credit, reportDate));
    }

    @Test
    public void creditMaxReportDateTest() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity credit = eavBaseFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);

        //TODO: добавить Assert
        baseEntityService.getMaxReportDate(credit, reportDate);
    }

    @Test
    public void creditMinReportDateTest() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2017, 1, 1);

        BaseEntity credit = eavBaseFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);

        //TODO: добавить Assert
        baseEntityService.getMinReportDate(credit, reportDate);
    }

}