package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.factory.EavDataFactory;
import kz.bsbnb.usci.core.service.EavHubService;
import kz.bsbnb.usci.core.service.EavXmlService;
import kz.bsbnb.usci.core.service.impl.EavXmlServiceImpl;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import kz.bsbnb.usci.model.eav.base.OperType;
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

import java.sql.SQLException;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EavXmlServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(EavXmlServiceTest.class);

    @Autowired
    private EavDataFactory eavDataFactory;
    @Autowired
    private EavXmlService eavXmlService;
    @Autowired
    private EavHubService eavHubService;

    @Before
    public void setUp() {
    }

    @Test
    public void test0() throws SQLException {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setOperation(OperType.INSERT);

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        BaseEntity currency = eavDataFactory.createBaseEntity("ref_currency", reportDate, respondentId, batchId);
        currency.put("short_name", new BaseValue("KZT"));
        credit.put("currency", new BaseValue(currency));

        credit.put("interest_rate_yearly", new BaseValue(24D));
        credit.put("actual_issue_date", new BaseValue(LocalDate.of(2017, 8, 31)));

        BaseEntity creditPurpose = eavDataFactory.createBaseEntity("ref_credit_purpose", reportDate, respondentId, batchId);
        currency.put("code", new BaseValue("01"));
        credit.put("credit_purpose", new BaseValue(creditPurpose));

        BaseEntity creditObject = eavDataFactory.createBaseEntity("ref_credit_object", reportDate, respondentId, batchId);
        currency.put("code", new BaseValue("03"));
        credit.put("credit_object", new BaseValue(creditObject));

        credit.put("amount", new BaseValue(100000D));

        BaseEntity financeSource = eavDataFactory.createBaseEntity("ref_finance_source", reportDate, respondentId, batchId);
        financeSource.put("code", new BaseValue("01"));
        credit.put("finance_source", new BaseValue(financeSource));

        credit.put("has_currency_earn", new BaseValue(Boolean.FALSE));

        //---- залоги
        BaseSet pledges = eavDataFactory.createBaseSet("pledge");

        BaseEntity pledgeType10 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, respondentId, batchId);
        pledgeType10.put("code", new BaseValue("10"));

        BaseEntity pledgeType18 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, respondentId, batchId);
        pledgeType18.put("code", new BaseValue("18"));

        BaseEntity pledge0 = eavDataFactory.createBaseEntity("pledge", reportDate, respondentId, batchId);
        pledge0.put("contract", new BaseValue("KD0014619/GR1"));
        pledge0.put("pledge_type", new BaseValue(pledgeType10));
        pledge0.put("value", new BaseValue(6000000d));

        BaseEntity pledge1 = eavDataFactory.createBaseEntity("pledge", reportDate, respondentId, batchId);
        pledge1.put("contract", new BaseValue("KD0014619/1"));
        pledge1.put("pledge_type", new BaseValue(pledgeType18));
        pledge1.put("value", new BaseValue(15127000d));

        pledges.put(new BaseValue(pledge0));
        pledges.put(new BaseValue(pledge1));

        credit.put("pledges", new BaseValue(pledges));

        /*BaseEntity portfolio = eavDataFactory.createBaseEntity("portfolio", reportDate, respondentId, batchId);

        BaseEntity portfolioMsfo = eavDataFactory.createBaseEntity("ref_portfolio", reportDate, respondentId, batchId);
        portfolioMsfo.put("code", new BaseValue("0933"));

        portfolio.put("portfolio_msfo", new BaseValue(portfolioMsfo));
        credit.put("portfolio", new BaseValue(portfolio));*/

        eavXmlService.process(credit);
    }

    @Test
    public void test1() {
        Long respondentId = 245L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));

        String key = eavHubService.getKeyString(primaryContract);

        logger.info("eav hub key: " + key);

        Assert.assertEquals("AICC.1354927\\|~~~\\|31.08.2017", key);

        //Long entityId = eavHubService.find(primaryContract);
    }

}