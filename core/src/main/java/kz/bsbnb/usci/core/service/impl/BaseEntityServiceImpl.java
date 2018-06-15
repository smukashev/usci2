package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.BaseEntityService;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.meta.MetaClass;
import kz.bsbnb.usci.util.Converter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author BSB
 */

@Service
public class BaseEntityServiceImpl implements BaseEntityService {
    private NamedParameterJdbcTemplate npJdbcTemplate;

    public BaseEntityServiceImpl(NamedParameterJdbcTemplate npJdbcTemplate) {
        this.npJdbcTemplate = npJdbcTemplate;
    }

    /**
     * метод проверят имеется ли сущность за отчетную дату в таблице БД
     * запрос выполняется за INDEX RANGE SCAN, COST = 1
     * (сочетание ENTITY_ID, REPORT_DATE, CREDITOR_ID является PRIMARY KEY)
     * */
    @Override
    public boolean existsBaseEntity(BaseEntity baseEntity, LocalDate reportDate) {
        if (baseEntity.getId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id");
        if (baseEntity.getRespondentId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id респондента");
        if (baseEntity.getMetaClass() == null)
            throw new IllegalArgumentException("У сущности отсутствует metaClass");

        MetaClass metaClass = baseEntity.getMetaClass();

        String query = "select ENTITY_ID\n" +
                "  from $schema.$table\n" +
                " where REPORT_DATE = :reportDate\n" +
                "   and ENTITY_ID = :entityId\n" +
                "   and CREDITOR_ID = :respondentId\n";

        query = query.replace("$schema", metaClass.getSchemaData());
        query = query.replace("$table", metaClass.getTableName());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportDate", Converter.convertToSqlDate(reportDate));
        params.addValue("entityId", baseEntity.getId());
        params.addValue("respondentId", baseEntity.getRespondentId());

        List<Long> rows = npJdbcTemplate.queryForList(query, params, Long.class);
        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E91, baseEntity));

        return rows.size() == 1;
    }

    /**
     * TODO: добавить описание
     * */
    @Override
    public LocalDate getMaxReportDate(BaseEntity baseEntity, LocalDate reportDate) {
        if (baseEntity.getId() == null || baseEntity.getId() < 1)
            throw new IllegalArgumentException("У сущности отсутствует id");
        if (baseEntity.getRespondentId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id респондента");
        if (baseEntity.getMetaClass() == null)
            throw new IllegalArgumentException("У сущности отсутствует metaClass");

        MetaClass metaClass = baseEntity.getMetaClass();

        String query = "select max(REPORT_DATE) REPORT_DATE\n" +
                "  from $schema.$table\n" +
                " where REPORT_DATE <= :reportDate\n" +
                "   and ENTITY_ID = :entityId\n" +
                "   and CREDITOR_ID = :respondentId\n";

        query = query.replace("$schema", metaClass.getSchemaData());
        query = query.replace("$table", metaClass.getTableName());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportDate", Converter.convertToSqlDate(reportDate));
        params.addValue("entityId", baseEntity.getId());
        params.addValue("respondentId", baseEntity.getRespondentId());

        List<LocalDate> rows = npJdbcTemplate.queryForList(query, params, LocalDate.class);
        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E91, baseEntity));

        return rows.get(0);
    }

    /**
     * TODO: добавить описание
     * */
    @Override
    public LocalDate getMinReportDate(BaseEntity baseEntity, LocalDate reportDate) {
        if (baseEntity.getId() == null || baseEntity.getId() < 1)
            throw new IllegalArgumentException("У сущности отсутствует id");
        if (baseEntity.getRespondentId() == null)
            throw new IllegalArgumentException("У сущности отсутствует id респондента");
        if (baseEntity.getMetaClass() == null)
            throw new IllegalArgumentException("У сущности отсутствует metaClass");

        MetaClass metaClass = baseEntity.getMetaClass();

        String query = "select min(REPORT_DATE) REPORT_DATE\n" +
                "  from $schema.$table\n" +
                " where REPORT_DATE > :reportDate\n" +
                "   and ENTITY_ID = :entityId\n" +
                "   and CREDITOR_ID = :respondentId\n";

        query = query.replace("$schema", metaClass.getSchemaData());
        query = query.replace("$table", metaClass.getTableName());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportDate", Converter.convertToSqlDate(reportDate));
        params.addValue("entityId", baseEntity.getId());
        params.addValue("respondentId", baseEntity.getRespondentId());

        List<LocalDate> rows = npJdbcTemplate.queryForList(query, params, LocalDate.class);
        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E91, baseEntity));

        return rows.get(0);
    }

}
