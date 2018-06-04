package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.service.EavDataService;
import kz.bsbnb.usci.core.factory.EavDataFactory;
import kz.bsbnb.usci.model.eav.data.EavDataEntity;
import kz.bsbnb.usci.model.eav.data.DataOperationType;
import kz.bsbnb.usci.model.eav.data.EavDataSimple;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EavXmlServiceTest {
    @Autowired
    private EavDataFactory eavDataFactory;
    @Autowired
    private EavDataService eavDataService;

    @Before
    public void setUp() {
    }

    @Test
    public void test0() {
        long creditorId = 2;
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        EavDataEntity credit = eavDataFactory.createDataEntity("credit");
        credit.setOperation(DataOperationType.INSERT);
        credit.setReportDate(reportDate);

        EavDataEntity contract = eavDataFactory.createDataEntity("contract");
        contract.put("no", new EavDataSimple("AICC.1354927"));
        contract.put("date", new EavDataSimple(LocalDate.of(2017, 8, 31)));
        credit.put("contract", contract);

        EavDataEntity currency = eavDataFactory.createDataEntity("ref_currency");
        currency.put("short_name", new EavDataSimple("KZT"));
        credit.put("currency", currency);

        credit.put("interest_rate_yearly", new EavDataSimple(24D));
        credit.put("actual_issue_date", new EavDataSimple(LocalDate.of(2017, 8, 31)));

        EavDataEntity creditPurpose = eavDataFactory.createDataEntity("ref_credit_purpose");
        currency.put("code", new EavDataSimple("01"));
        credit.put("credit_purpose", creditPurpose);

        EavDataEntity creditObject = eavDataFactory.createDataEntity("ref_credit_object");
        currency.put("code", new EavDataSimple("03"));
        credit.put("credit_object", creditObject);

        credit.put("amount", new EavDataSimple(100000D));

        EavDataEntity financeSource = eavDataFactory.createDataEntity("ref_finance_source");
        currency.put("code", new EavDataSimple("01"));
        credit.put("finance_source", financeSource);

        credit.put("has_currency_earn", new EavDataSimple(Boolean.FALSE));

        //TODO: creditor branch
        //TODO: portfolio, portfolio_msfo,

        eavDataService.process(credit);
    }



}