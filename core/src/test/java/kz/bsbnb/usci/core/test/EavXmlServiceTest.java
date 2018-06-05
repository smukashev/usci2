package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.service.EavDataService;
import kz.bsbnb.usci.core.factory.EavDataFactory;
import kz.bsbnb.usci.core.service.EavXmlService;
import kz.bsbnb.usci.model.eav.data.BaseEntity;
import kz.bsbnb.usci.model.eav.data.OperType;
import kz.bsbnb.usci.model.eav.data.BaseSimple;

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
    private EavXmlService eavXmlService;

    @Before
    public void setUp() {
    }

    @Test
    public void test0() {
        BaseEntity credit = eavDataFactory.createBaseEntity("credit");
        credit.setId(2L);
        credit.setRespondentId(245L);
        credit.setOperation(OperType.INSERT);
        credit.setReportDate(LocalDate.of(2018, 1, 1));

        BaseEntity contract = eavDataFactory.createBaseEntity("contract");
        contract.put("no", new BaseSimple("AICC.1354927"));
        contract.put("date", new BaseSimple(LocalDate.of(2017, 8, 31)));
        credit.put("contract", contract);

        BaseEntity currency = eavDataFactory.createBaseEntity("ref_currency");
        currency.put("short_name", new BaseSimple("KZT"));
        credit.put("currency", currency);

        credit.put("interest_rate_yearly", new BaseSimple(24D));
        credit.put("actual_issue_date", new BaseSimple(LocalDate.of(2017, 8, 31)));

        BaseEntity creditPurpose = eavDataFactory.createBaseEntity("ref_credit_purpose");
        currency.put("code", new BaseSimple("01"));
        credit.put("credit_purpose", creditPurpose);

        BaseEntity creditObject = eavDataFactory.createBaseEntity("ref_credit_object");
        currency.put("code", new BaseSimple("03"));
        credit.put("credit_object", creditObject);

        credit.put("amount", new BaseSimple(100000D));

        BaseEntity financeSource = eavDataFactory.createBaseEntity("ref_finance_source");
        financeSource.put("code", new BaseSimple("01"));
        credit.put("finance_source", financeSource);

        credit.put("has_currency_earn", new BaseSimple(Boolean.FALSE));

        //TODO: creditor branch
        //TODO: portfolio, portfolio_msfo,

        eavXmlService.process(credit);
    }



}