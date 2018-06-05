package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.EavDataService;
import kz.bsbnb.usci.core.service.EavXmlService;
import kz.bsbnb.usci.model.eav.data.BaseEntity;
import kz.bsbnb.usci.model.eav.data.BaseSet;
import kz.bsbnb.usci.model.eav.data.BaseSimple;
import kz.bsbnb.usci.model.eav.data.BaseType;
import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaType;
import kz.bsbnb.usci.util.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EavXmlServiceImpl implements EavXmlService {
    private static final Logger logger = LoggerFactory.getLogger(EavDataService.class);
    private final NamedParameterJdbcTemplate npJdbcTemplate;

    public EavXmlServiceImpl(NamedParameterJdbcTemplate npJdbcTemplate) {
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Override
    //TODO: желательно чтобы эта переменная была final
    public void process(BaseEntity saving) {
        logger.info("eav_xml: processing entity");

        //TODO:

        insEavDataEntityToDb(saving);
    }

    private void insEavDataEntityToDb(BaseEntity baseEntity) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(npJdbcTemplate.getJdbcTemplate());
        simpleJdbcInsert.setSchemaName(baseEntity.getMetaClass().getSchemaXml());
        simpleJdbcInsert.setTableName(baseEntity.getMetaClass().getTableName());

        MapSqlParameterSource params = new MapSqlParameterSource();

        // обязательные поля в любой таблице
        List<String> columns = new ArrayList<>(Arrays.asList("entity_id", "creditor_id", "report_date"));

        params.addValue("entity_id", baseEntity.getId());
        params.addValue("creditor_id", baseEntity.getRespondentId());
        params.addValue("report_date", Converter.convertToSqlDate(baseEntity.getReportDate()));

        baseEntity.getValues().forEach((attributeName, baseValue) -> {
            MetaAttribute metaAttribute = baseValue.getMetaAttribute();

            columns.add(metaAttribute.getColumnName());

            params.addValue(metaAttribute.getColumnName(), convertEavDataToRmValue(baseValue));
        });

        simpleJdbcInsert.setColumnNames(columns);

        int count = simpleJdbcInsert.execute(params);
        if (count == 0)
            throw new IllegalArgumentException("Ошибка завершения DML операций");
    }

    // конвертирует EAV значение в значение реляционной таблицы
    // для комплексных сетов берем только id сущностей, конвертируем коллекцию в set дабы не было дубликатов далее обратно конвертируем в обычный массив
    // для обычных сетов берем массив значений
    // для скалярных сущностей берем только id самой сущности
    // для скалярных примитивных значений берем само значение
    private Object convertEavDataToRmValue(BaseType baseType) {
        MetaType metaType = baseType.getMetaAttribute().getMetaType();

        Object value;
        if (metaType.isSet()) {
            BaseSet baseSet = (BaseSet) baseType;
            if (baseType.isComplex())
                value = new ArrayList<>(baseSet.getValues().stream().map(ed -> ((BaseEntity) ed).getId()).collect(Collectors.toSet()));
            else
                value = new ArrayList<>(baseSet.getValues().stream().map(ed -> ((BaseSimple)ed).getValue()).collect(Collectors.toSet()));
        }
        else if (metaType.isComplex())
            value = ((BaseEntity) baseType).getId();
        else
            value = ((BaseSimple) baseType).getRmValue();

        return value;
    }

}
