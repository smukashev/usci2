package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.service.BaseEntityLoadService;
import kz.bsbnb.usci.core.factory.EavDataFactory;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseEntityLoadServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityLoadServiceTest.class);

    @Autowired
    private EavDataFactory eavDataFactory;
    @Autowired
    private BaseEntityLoadService baseEntityLoadService;

    @Before
    public void setUp() {
        //
    }

    @Test
    public void loadBaseEntityTest() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);

        credit = baseEntityLoadService.loadBaseEntity(credit.getId(), credit.getRespondentId(), credit.getMetaClass(), reportDate, reportDate);

        logger.info(credit.toString());
    }



}