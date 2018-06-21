package kz.bsbnb.usci.core.test;

import kz.bsbnb.usci.core.factory.EavDataFactory;
import kz.bsbnb.usci.core.service.BaseEntityProcessor;
import kz.bsbnb.usci.core.service.EavHubService;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import kz.bsbnb.usci.model.eav.base.OperType;
import kz.bsbnb.usci.model.eav.base.BaseValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseEntityProcessorTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorTest.class);

    @Autowired
    private EavDataFactory eavDataFactory;
    @Autowired
    private BaseEntityProcessor baseEntityProcessor;

    @Before
    public void setUp() {
        //
    }

    @Test
    public void processBaseEntityTest() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = Math.round(Math.random());
        LocalDate reportDate = LocalDate.of(2018, 1, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setOperation(OperType.INSERT);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        BaseEntity currency = eavDataFactory.createBaseEntity("ref_currency", reportDate, 0L, batchId);
        currency.put("short_name", new BaseValue("KZT"));
        credit.put("currency", new BaseValue(currency));

        credit.put("interest_rate_yearly", new BaseValue(24D));
        credit.put("actual_issue_date", new BaseValue(LocalDate.of(2017, 8, 31)));

        BaseEntity creditPurpose = eavDataFactory.createBaseEntity("ref_credit_purpose", reportDate, 0L, batchId);
        currency.put("code", new BaseValue("01"));
        credit.put("credit_purpose", new BaseValue(creditPurpose));

        BaseEntity creditObject = eavDataFactory.createBaseEntity("ref_credit_object", reportDate, 0L, batchId);
        currency.put("code", new BaseValue("03"));
        credit.put("credit_object", new BaseValue(creditObject));

        credit.put("amount", new BaseValue(100000D));

        BaseEntity financeSource = eavDataFactory.createBaseEntity("ref_finance_source", reportDate, 0L, batchId);
        financeSource.put("code", new BaseValue("01"));
        credit.put("finance_source", new BaseValue(financeSource));

        credit.put("has_currency_earn", new BaseValue(Boolean.FALSE));

        //---- залоги
        BaseSet pledges = eavDataFactory.createBaseSet("pledge");

        BaseEntity pledgeType10 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, 0L, batchId);
        pledgeType10.put("code", new BaseValue("10"));

        BaseEntity pledgeType18 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, 0L, batchId);
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

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void applyBaseEntityAdvancedTest() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = 6L;
        LocalDate reportDate = LocalDate.of(2018, 7, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);
        credit.setOperation(OperType.UPDATE);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        creditor.setId(2384L);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        BaseEntity currency = eavDataFactory.createBaseEntity("ref_currency", reportDate, 0L, batchId);
        currency.put("short_name", new BaseValue("USD"));
        credit.put("currency", new BaseValue(currency));

        credit.put("interest_rate_yearly", new BaseValue(24D));
        credit.put("actual_issue_date", new BaseValue(LocalDate.of(2017, 8, 31)));

        BaseEntity creditPurpose = eavDataFactory.createBaseEntity("ref_credit_purpose", reportDate, 0L, batchId);
        creditPurpose.put("code", new BaseValue("01"));
        credit.put("credit_purpose", new BaseValue(creditPurpose));

        BaseEntity creditObject = eavDataFactory.createBaseEntity("ref_credit_object", reportDate, 0L, batchId);
        creditObject.put("code", new BaseValue("03"));
        credit.put("credit_object", new BaseValue(creditObject));

        //credit.put("amount", new BaseValue(200000D));

        BaseEntity financeSource = eavDataFactory.createBaseEntity("ref_finance_source", reportDate, 0L, batchId);
        financeSource.put("code", new BaseValue("01"));
        credit.put("finance_source", new BaseValue(financeSource));

        credit.put("has_currency_earn", new BaseValue(Boolean.FALSE));

        //---- залоги
        BaseSet pledges = eavDataFactory.createBaseSet("pledge");

        BaseEntity pledgeType10 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, 0L, batchId);
        pledgeType10.put("code", new BaseValue("10"));

        BaseEntity pledgeType18 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, 0L, batchId);
        pledgeType18.put("code", new BaseValue("18"));

        BaseEntity pledge0 = eavDataFactory.createBaseEntity("pledge", reportDate, respondentId, batchId);
        pledge0.put("contract", new BaseValue("KD0014619/GR1"));
        pledge0.put("pledge_type", new BaseValue(pledgeType18));
        pledge0.put("value", new BaseValue(6d));

        BaseEntity pledge1 = eavDataFactory.createBaseEntity("pledge", reportDate, respondentId, batchId);
        pledge1.put("contract", new BaseValue("SD0014619/2"));
        pledge1.put("pledge_type", new BaseValue(pledgeType10));
        pledge1.put("value", new BaseValue(7d));

        pledges.put(new BaseValue(pledge0));
        pledges.put(new BaseValue(pledge1));

        credit.put("pledges", new BaseValue(pledges));

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void creditNullActualIssueDateTest() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = 8L;
        LocalDate reportDate = LocalDate.of(2018, 5, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);
        credit.setOperation(OperType.UPDATE);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        creditor.setId(2384L);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        credit.put("actual_issue_date", new BaseValue());

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void creditNullCurrencyTest() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = 9L;
        LocalDate reportDate = LocalDate.of(2018, 8, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);
        credit.setOperation(OperType.UPDATE);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        creditor.setId(2384L);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        credit.put("currency", new BaseValue());

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void applyBaseEntityAdvancedTest2() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = 5L;
        LocalDate reportDate = LocalDate.of(2018, 4, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);
        credit.setOperation(OperType.UPDATE);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        creditor.setId(2384L);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        //---- залоги
        BaseSet pledges = eavDataFactory.createBaseSet("pledge");

        BaseEntity pledgeType10 = eavDataFactory.createBaseEntity("ref_pledge_type", reportDate, 0L, batchId);
        pledgeType10.put("code", new BaseValue("47"));

        BaseEntity pledge0 = eavDataFactory.createBaseEntity("pledge", reportDate, respondentId, batchId);
        //pledge0.put("contract", new BaseValue("KD0014619/GR1"));
        pledge0.put("pledge_type", new BaseValue(pledgeType10));
        pledge0.put("value", new BaseValue(5d));

        credit.put("pledges", new BaseValue(pledges));

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void creditChangeRemainsTest() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = 7L;
        LocalDate reportDate = LocalDate.of(2018, 9, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);
        credit.setOperation(OperType.UPDATE);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        creditor.setId(2384L);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        //change
        BaseEntity ba1403191 = eavDataFactory.createBaseEntity("ref_balance_account", reportDate, 0L, batchId);
        ba1403191.put("no_", new BaseValue("1403191"));

        BaseEntity change = eavDataFactory.createBaseEntity("change", reportDate, respondentId, batchId);
        BaseEntity remains = eavDataFactory.createBaseEntity("remains", reportDate, respondentId, batchId);
        BaseEntity debt = eavDataFactory.createBaseEntity("remains_debt", reportDate, respondentId, batchId);

        BaseEntity current = eavDataFactory.createBaseEntity("remains_debt_current", reportDate, respondentId, batchId);
        current.put("value", new BaseValue(195376D));
        current.put("value_currency", new BaseValue(0D));
        current.put("balance_account", new BaseValue(ba1403191));

        change.put("remains", new BaseValue(remains));
        remains.put("debt", new BaseValue(debt));
        debt.put("current", new BaseValue(current));
        credit.put("change", new BaseValue(change));

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void creditChangeTurnoverTest() throws SQLException {
        Long respondentId = 2384L;
        Long batchId = 16L;
        LocalDate reportDate = LocalDate.of(2018, 11, 1);

        BaseEntity credit = eavDataFactory.createBaseEntity("credit", reportDate, respondentId, batchId);
        credit.setId(693L);
        credit.setOperation(OperType.UPDATE);

        BaseEntity creditor = eavDataFactory.createBaseEntity("ref_creditor", reportDate, respondentId, batchId);
        creditor.setId(2384L);
        BaseSet creditorDocs = eavDataFactory.createBaseSet("document");

        BaseEntity docType = eavDataFactory.createBaseEntity("ref_doc_type", reportDate, 0L, batchId);
        docType.put("code", new BaseValue("15"));

        BaseEntity document = eavDataFactory.createBaseEntity("document", reportDate, respondentId, batchId);
        document.put("doc_type", new BaseValue(docType));
        document.put("no", new BaseValue("ATYNKZKA"));

        creditorDocs.put(new BaseValue(document));
        credit.put("creditor", new BaseValue(creditor));

        BaseEntity primaryContract = eavDataFactory.createBaseEntity("primary_contract", reportDate, respondentId, batchId);
        primaryContract.put("no", new BaseValue("AICC.1354927"));
        primaryContract.put("date", new BaseValue(LocalDate.of(2017, 8, 31)));
        credit.put("primary_contract", new BaseValue(primaryContract));

        //change
        BaseEntity change = eavDataFactory.createBaseEntity("change", reportDate, respondentId, batchId);
        BaseEntity turnover = eavDataFactory.createBaseEntity("turnover", reportDate, respondentId, batchId);
        BaseEntity issue = eavDataFactory.createBaseEntity("turnover_issue", reportDate, respondentId, batchId);

        BaseEntity debt = eavDataFactory.createBaseEntity("turnover_issue_debt", reportDate, respondentId, batchId);
        debt.put("amount", new BaseValue(195376D));
        debt.put("amount_currency", new BaseValue(455D));

        change.put("turnover", new BaseValue(turnover));
        turnover.put("issue", new BaseValue(issue));
        issue.put("debt", new BaseValue(debt));
        credit.put("change", new BaseValue(change));

        baseEntityProcessor.processBaseEntity(credit, reportDate);
    }

    @Test
    public void prepareBaseEntityTest() {
        //TODO: реализовать
    }


}