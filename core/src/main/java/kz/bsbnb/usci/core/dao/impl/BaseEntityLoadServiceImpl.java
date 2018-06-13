package kz.bsbnb.usci.core.dao.impl;

import kz.bsbnb.usci.core.dao.BaseEntityLoadService;
import kz.bsbnb.usci.core.model.EavDbSchema;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import kz.bsbnb.usci.model.eav.base.BaseValue;
import kz.bsbnb.usci.model.eav.meta.*;
import kz.bsbnb.usci.util.Converter;
import oracle.jdbc.OracleArray;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class BaseEntityLoadServiceImpl implements BaseEntityLoadService {
    private final NamedParameterJdbcTemplate npJdbcTemplate;

    public BaseEntityLoadServiceImpl(NamedParameterJdbcTemplate npJdbcTemplate) {
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Override
    public BaseEntity loadBaseEntity(BaseEntity baseEntity, LocalDate reportDate) {
        if (baseEntity.getId() == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E93));

        if (baseEntity.getRespondentId() == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E93));

        MetaClass metaClass = baseEntity.getMetaClass();

        java.sql.Date sqlReportDate = Converter.convertToSqlDate(reportDate);

        StringBuilder sb = new StringBuilder("select $tableAlias.ENTITY_ID,\n");
        sb.append("$tableAlias.REPORT_DATE,\n");
        sb.append("$tableAlias.CREDITOR_ID,\n");
        sb.append("$tableAlias.BATCH_ID");

        String tableAlias = metaClass.getClassName();

        metaClass.getAttributes().forEach(attribute -> {
            sb.append(",\n");
            sb.append(tableAlias).append(".");
            sb.append(attribute.getColumnName());
        });

        sb.append("\n");
        sb.append("from ");
        sb.append(String.join(".", metaClass.getSchemaData(), metaClass.getTableName()));
        sb.append(" ").append(tableAlias);
        sb.append("\n");

        sb.append("where $tableAlias.ENTITY_ID = :entityId\n");
        sb.append("  and $tableAlias.CREDITOR_ID = :respondentId\n");

        sb.append("  and $tableAlias.REPORT_DATE = \n" +
                  "      (select max($subTableAlias.REPORT_DATE)\n" +
                  "         from $schema.$tableName $subTableAlias\n" +
                  "        where $subTableAlias.ENTITY_ID = $tableAlias.ENTITY_ID\n" +
                  "          and $subTableAlias.CREDITOR_ID = $tableAlias.CREDITOR_ID\n" +
                  "          and $subTableAlias.REPORT_DATE <= :reportDate)\n");

        //TODO: поиск по minReportDate, isFinal

        String query = sb.toString();

        query = query.replace("$tableAlias", tableAlias);
        query = query.replace("$schema", metaClass.getSchemaData());
        query = query.replace("$tableName", metaClass.getTableName());
        query = query.replace("$subTableAlias", "sub_" + metaClass.getTableName().toLowerCase());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("entityId", baseEntity.getId());
        params.addValue("respondentId", baseEntity.getRespondentId());
        params.addValue("reportDate", sqlReportDate);

        List<Map<String, Object>> rows = npJdbcTemplate.queryForList(query, params);

        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E91, baseEntity));

        if (rows.size() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E92, baseEntity));

        Map<String, Object> values = rows.get(0);
        fillEntityAttributes(values, baseEntity, reportDate);

        return baseEntity;
    }

    private BaseEntity fillEntityAttributes(Map<String, Object> values, BaseEntity baseEntity, LocalDate reportDate) {
        try {
            MetaClass metaClass = baseEntity.getMetaClass();

            baseEntity.setReportDate(Converter.convertToLocalDate((java.sql.Timestamp) values.get("REPORT_DATE")));
            baseEntity.setBatchId(Converter.convertToLong(values.get("BATCH_ID")));
            baseEntity.setRespondentId(Converter.convertToLong(values.get("CREDITOR_ID")));
            baseEntity.setId(Converter.convertToLong(values.get("ENTITY_ID")));

            for (MetaAttribute attribute : metaClass.getAttributes()) {
                MetaType metaType = attribute.getMetaType();

                Object sqlValue = values.get(attribute.getColumnName());
                if (sqlValue == null)
                    continue;

                Object javaValue;

                if (metaType.isComplex()) {
                    if (metaType.isSet()) {
                        BaseSet baseSet = loadComplexSet(baseEntity, attribute, reportDate);
                        OracleArray oracleArray = (OracleArray) sqlValue;
                        if (baseSet.getValueCount() != oracleArray.length())
                            throw new IllegalStateException("Кол-во элементов в мнжестве не верное " + baseSet);

                        javaValue = baseSet;
                    } else {
                        MetaClass childMetClass = (MetaClass) attribute.getMetaType();
                        BaseEntity childBaseEntity = new BaseEntity(childMetClass);
                        childBaseEntity.setId(Converter.convertToLong(values.get(attribute.getColumnName())));
                        childBaseEntity.setRespondentId(childMetClass.isDictionary()? 0: baseEntity.getRespondentId());

                        javaValue = loadBaseEntity(childBaseEntity, reportDate);
                    }
                } else {
                    if (metaType.isSet()) {
                        throw new UnsupportedOperationException("Not yet implemented");//TODO:
                    } else
                        javaValue = convertRmValueToJavaType(attribute, sqlValue);
                }

                if (javaValue != null)
                    baseEntity.put(attribute.getName(), new BaseValue(javaValue));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return baseEntity;
    }

    private BaseSet loadComplexSet(BaseEntity parentBaseEntity, MetaAttribute metaAttribute, LocalDate reportDate) {
        MetaSet metaSet = (MetaSet)metaAttribute.getMetaType();
        MetaClass metaClass = (MetaClass) metaSet.getMetaType();
        String tableAlias = metaClass.getClassName().toLowerCase();

        MetaClass parentMetaClass = parentBaseEntity.getMetaClass();
        String parentTableAlias = parentMetaClass.getClassName().toLowerCase();

        StringBuilder sb = new StringBuilder("select $tableAlias.ENTITY_ID,\n");
        sb.append("$tableAlias.REPORT_DATE,\n");
        sb.append("$tableAlias.CREDITOR_ID,\n");
        sb.append("$tableAlias.BATCH_ID");

        metaClass.getAttributes().forEach(attribute -> {
            sb.append(",\n");
            sb.append(tableAlias).append(".");
            sb.append(attribute.getColumnName());
        });

        sb.append("\n");
        sb.append("from ");
        sb.append(String.join(".", metaClass.getSchemaData(), metaClass.getTableName()));
        sb.append(" ").append(tableAlias).append(",\n");

        sb.append(String.join(".", parentMetaClass.getSchemaData(), parentMetaClass.getTableName()));
        sb.append(" ").append(parentTableAlias).append(",\n");

        sb.append("table($parentTableAlias.$arrayColumnName) $arrayTableAlias\n");

        sb.append("where $parentTableAlias.ENTITY_ID = :parentEntityId\n");
        sb.append("  and $parentTableAlias.CREDITOR_ID = :respondentId\n");
        sb.append("  and $parentTableAlias.REPORT_DATE = :parentReportDate\n");

        sb.append("  and $tableAlias.ENTITY_ID = $arrayTableAlias.COLUMN_VALUE\n");
        sb.append("  and $tableAlias.CREDITOR_ID = :respondentId\n");
        sb.append("  and $tableAlias.REPORT_DATE = \n" +
                  "      (select max($subTableAlias.REPORT_DATE)\n" +
                  "         from $schema.$tableName $subTableAlias\n" +
                  "        where $subTableAlias.ENTITY_ID = $tableAlias.ENTITY_ID\n" +
                  "          and $subTableAlias.CREDITOR_ID = $tableAlias.CREDITOR_ID\n" +
                  "          and $subTableAlias.REPORT_DATE <= :reportDate)\n");

        String query = sb.toString();

        query = query.replace("$schema", metaClass.getSchemaData());
        query = query.replace("$tableName", metaClass.getTableName());
        query = query.replace("$tableAlias", tableAlias);
        query = query.replace("$parentTableAlias", parentTableAlias);
        query = query.replace("$arrayTableAlias", metaAttribute.getColumnName().toLowerCase());
        query = query.replace("$subTableAlias", "sub_" + metaClass.getTableName().toLowerCase());
        query = query.replace("$arrayColumnName", metaAttribute.getColumnName());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("parentEntityId", parentBaseEntity.getId());
        params.addValue("respondentId", parentBaseEntity.getRespondentId());
        params.addValue("parentReportDate", Converter.convertToSqlDate(parentBaseEntity.getReportDate()));
        params.addValue("reportDate", Converter.convertToSqlDate(reportDate));

        //TODO: поиск по maxReportDate, isFinal
        List<Map<String, Object>> rows = npJdbcTemplate.queryForList(query, params);

        if (rows.size() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E92, parentBaseEntity));

        BaseSet baseSet = new BaseSet(metaClass);
        for (Map<String, Object> row : rows) {
            BaseEntity baseEntity = new BaseEntity(metaClass);

            baseEntity = fillEntityAttributes(row, baseEntity, reportDate);

            baseSet.put(new BaseValue(baseEntity));
        }

        return baseSet;
    }


    // конвертация значений jdbc формата в формат java
    // пояснения: BOOLEAN хранится в формате varchar2(1)
    // дата конвертируется из java.sql.Timestamp в LocalDate
    // числа конвертируются из NUMBER в BigDecimal и потом в Long, Double и тд
    // String получаем как есть (varchar2)
    private Object convertRmValueToJavaType(MetaAttribute attribute, Object sqlValue) {
        MetaType metaType = attribute.getMetaType();

        if (metaType.isComplex() || metaType.isSet())
            throw new IllegalArgumentException("Метод предназначен только для конвертаций примитивных типов данных");

        if (sqlValue == null)
            throw new IllegalArgumentException("Ошибка значения NULL");

        Object javaValue;

        MetaValue metaValue = (MetaValue) metaType;

        switch (metaValue.getMetaDataType()) {
            case DATE:
                javaValue = Converter.convertToLocalDate((java.sql.Timestamp)sqlValue);
                break;
            case BOOLEAN:
                javaValue = sqlValue.equals("1")? Boolean.TRUE: Boolean.FALSE;
                break;
            case DOUBLE:
                javaValue = Converter.convertToDouble(sqlValue);
                break;
            case INTEGER:
                javaValue = Converter.convertToInt(sqlValue);
                break;
            case STRING:
                javaValue = sqlValue;
                break;
            default:
                throw new UnsupportedOperationException("Unresolved metaDataType");
        }

        return javaValue;
    }


}
