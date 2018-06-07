package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.EavXmlService;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.base.*;
import kz.bsbnb.usci.model.eav.meta.*;
import kz.bsbnb.usci.util.Converter;
import oracle.jdbc.driver.OracleConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EavXmlServiceImpl implements EavXmlService {
    private static final Logger logger = LoggerFactory.getLogger(EavXmlServiceImpl.class);

    private final NamedParameterJdbcTemplate npJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public EavXmlServiceImpl(NamedParameterJdbcTemplate npJdbcTemplate, JdbcTemplate jdbcTemplate) {
        this.npJdbcTemplate = npJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public BaseEntity process(final BaseEntity baseEntitySaving) throws SQLException {
        logger.info("eav_xml: processing entity");

        //TODO: необходимо создавать baseEntityApplied
        //TODO: необходимо добавить комментарий по алгоритму

        // TODO: старый коммент Пропускает закрытые теги на новые сущности <tag/>
        for (String attributeName: baseEntitySaving.getAttributes()) {
            MetaAttribute metaAttribute = baseEntitySaving.getMetaAttribute(attributeName);
            MetaType metaType = metaAttribute.getMetaType();
            BaseValue baseValue = baseEntitySaving.getBaseValue(attributeName);

            if (baseValue != null && baseValue.getValue() != null && metaType.isComplex() && !metaAttribute.isImmutable())
                processBaseValue(metaAttribute, baseValue);
        }

        applyBaseEntityToDb(baseEntitySaving);

        return baseEntitySaving;
    }

    private void processBaseValue(MetaAttribute metaAttribute, final BaseValue baseValueSaving) throws SQLException {
        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E60));

        BaseContainer baseContainer = baseValueSaving.getBaseContainer();
        if (baseContainer != null && !(baseContainer instanceof BaseEntity))
            throw new IllegalStateException(Errors.compose(Errors.E59, metaAttribute.getName()));

        MetaType metaType = metaAttribute.getMetaType();

        if (!metaType.isComplex())
            throw new IllegalArgumentException("Данный метод обрабатывает только комплексные атрибуты");

        if (metaType.isSet()) {
            BaseSet childBaseSet = (BaseSet) baseValueSaving.getValue();

            for (BaseValue childBaseValue : childBaseSet.getValues())
                process((BaseEntity) childBaseValue.getValue());
        }
        else
            process((BaseEntity) baseValueSaving.getValue());
    }

    // подготовительные работы перед обработкой сущности:
    // присваиваем id всем найденным сущностям
    // TODO:
    public void prepare(final BaseEntity baseEntity) {
        //TODO:
    }

    //TODO: в будущем сделать метод общим для eavXmlService, eavDataService
    // инсерт сущности в базу => одна сущность = одна запись в базе
    // наименование таблицы и столбцов берем из мета данных
    // также не забываем что в любой таблице есть обязательные поля (см. код) помимо атрибутов мета класса
    private void applyBaseEntityToDb(final BaseEntity baseEntity) throws SQLException {
        if (baseEntity.getId() != 0)
            throw new IllegalArgumentException("Метод обрабатывает только новые сущности");

        MetaClass metaClass = baseEntity.getMetaClass();

        //TODO: брать сиквенс из мета классов
        Long newId = jdbcTemplate.queryForObject("select EAV_DATA.SEQ_EAV_BE_ENTITIES.NEXTVAL from dual", Long.class);
        baseEntity.setId(newId);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(npJdbcTemplate.getJdbcTemplate());
        simpleJdbcInsert.setSchemaName(metaClass.getSchemaXml());
        simpleJdbcInsert.setTableName(metaClass.getTableName());

        // обязательные поля в реляционной таблице: entityId, creditorId, reportDate, batchId
        Set<String> columns = new HashSet<>(Arrays.asList("ENTITY_ID", "CREDITOR_ID", "REPORT_DATE", "BATCH_ID"));
        columns.addAll(baseEntity.getAttributes().stream()
                .map(attributeName -> baseEntity.getMetaAttribute(attributeName).getColumnName())
                .collect(Collectors.toList()));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("BATCH_ID", baseEntity.getBatchId());
        params.addValue("ENTITY_ID", baseEntity.getId());
        params.addValue("CREDITOR_ID", baseEntity.getRespondentId());
        params.addValue("REPORT_DATE", Converter.convertToSqlDate(baseEntity.getReportDate()));

        for (String attributeName: baseEntity.getAttributes()) {
            BaseValue baseValue = baseEntity.getBaseValue(attributeName);
            MetaAttribute metaAttribute = baseEntity.getMetaAttribute(attributeName);

            Object sqlValue = convertBaseValueToRmValue(metaAttribute, baseValue);
            params.addValue(metaAttribute.getColumnName(), sqlValue);
        }

        simpleJdbcInsert.setColumnNames(new ArrayList<>(columns));

        int count = simpleJdbcInsert.execute(params);
        if (count == 0)
            throw new IllegalArgumentException("Ошибка завершения DML операций по таблице " + String.join(".", metaClass.getSchemaXml(), metaClass.getTableName()));
    }

    // конвертирует EAV значение в значение реляционной таблицы
    // для комплексных сетов берем только id сущностей, конвертируем коллекцию конвертируем в обычный массив
    // для обычных сетов берем массив значений
    // для скалярных сущностей берем только id самой сущности
    // для скалярных примитивных значений берем само значение
    private Object convertBaseValueToRmValue(MetaAttribute metaAttribute, BaseValue baseValue) throws SQLException {
        MetaType metaType = metaAttribute.getMetaType();

        Object value;
        if (metaType.isSet()) {
            Object array;
            BaseSet baseSet = (BaseSet) baseValue.getValue();
            if (metaType.isComplex())
                array = new ArrayList<>(baseSet.getValues().stream()
                        .map(ed -> ((BaseEntity) ed.getValue()).getId())
                        .collect(Collectors.toSet())).toArray();
            else
                array = new ArrayList<>(baseSet.getValues().stream()
                        .map(ed -> ((BaseValue)ed.getValue()).getValue())
                        .collect(Collectors.toSet())).toArray();

            // особенность Oracle, для создания массива обязательно пользоваться createARRAY а не createArrayOf
            // также необходимо получить соединение с базой spring утилитой иначе получим только прокси обьект
            Connection conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            OracleConnection oraConn = conn.unwrap(OracleConnection.class);

            value = oraConn.createARRAY(String.join(".", "EAV_XML", metaAttribute.getColumnType()), array);
        }
        else if (metaType.isComplex())
            value = ((BaseEntity) baseValue.getValue()).getId();
        else
            value = MetaDataType.convertToRmValue(((MetaValue) metaAttribute.getMetaType()).getMetaDataType(), baseValue.getValue());

        return value;
    }

}
