package kz.bsbnb.usci.core.dao.impl;

import kz.bsbnb.usci.core.dao.MetaClassDao;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.meta.*;
import kz.bsbnb.usci.util.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author BSB
 */

@Repository
public class MetaClassDaoImpl implements MetaClassDao {
    private final NamedParameterJdbcTemplate npJdbcTemplate;

    @Autowired
    public MetaClassDaoImpl(NamedParameterJdbcTemplate npJdbcTemplate) {
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Override
    public MetaClass load(String className) {
        MetaClass metaClass = new MetaClass(className);

        loadClass(metaClass);
        loadAttributes(metaClass);

        return metaClass;
    }

    @Override
    public List<MetaClass> loadAll() {
        List<MetaClass> metaClasses = npJdbcTemplate.query("select * from EAV_CORE.EAV_M_CLASSES", new MetaClassMapper());

        metaClasses.forEach(this::loadAttributes);

        return metaClasses;
    }

    @Override
    public MetaClass load(Long id) {
        if (id < 1)
            return null;

        MetaClass meta = new MetaClass();
        meta.setId(id);

        loadClass(meta);
        loadAttributes(meta);

        return meta;
    }

    private void loadClass(MetaClass metaClass) {
        String query;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (metaClass.getId() < 1) {
            if (metaClass.getClassName() == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E162));

            query = "select * from EAV_CORE.EAV_M_CLASSES where NAME = :className";
            params.addValue("className", metaClass.getClassName());
        } else {
            query = "select * from EAV_CORE.EAV_M_CLASSES where ID = :id";
            params.addValue("id", metaClass.getId());
        }

        metaClass = npJdbcTemplate.queryForObject(query, params, new MetaClassMapper(metaClass));

        loadAttributes(metaClass);
    }

    private void loadAttributes(MetaClass meta) {
        if (meta.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E164));

        meta.removeAttributes();

        loadSimpleAttributes(meta);
        loadSimpleArrays(meta);
        loadComplexAttributes(meta);
        loadComplexArrays(meta);
    }

    private void loadSimpleAttributes(MetaClass metaClass) {
        List<MetaAttribute> attributes = npJdbcTemplate.query("select * from EAV_CORE.EAV_M_SIMPLE_ATTRIBUTES where CLASS_ID = :classId",
            new MapSqlParameterSource("classId", metaClass.getId()), (rs, i) -> {
                MetaAttribute metaAttribute = new MetaAttribute(rs.getLong("id"));

                metaAttribute.setTitle(rs.getString("title"));
                metaAttribute.setName(rs.getString("name"));
                metaAttribute.setColumnName(rs.getString("column_name"));
                metaAttribute.setColumnType(rs.getString("column_type"));
                metaAttribute.setMetaType(new MetaValue(MetaDataType.valueOf(rs.getString("type_code"))));
                metaAttribute.setKey(rs.getBoolean("is_key"));
                metaAttribute.setNullable(rs.getBoolean("is_nullable"));
                metaAttribute.setFinal(rs.getBoolean("is_final"));
                metaAttribute.setImmutable(rs.getBoolean("is_immutable"));

                return metaAttribute;
            });

        for (MetaAttribute row : attributes)
            metaClass.setMetaAttribute(row.getName(), row);
    }

    private void loadSimpleArrays(MetaClass metaClass) {
        List<MetaAttribute> attributes = npJdbcTemplate.query("select * from EAV_CORE.EAV_M_SIMPLE_SET where CLASS_ID = :classId",
            new MapSqlParameterSource("classId", metaClass.getId()), (rs, i) -> {
                MetaAttribute metaAttribute = new MetaAttribute(rs.getLong("id"));

                metaAttribute.setName(rs.getString("name"));
                metaAttribute.setTitle(rs.getString("title"));
                metaAttribute.setColumnName(rs.getString("column_name"));
                metaAttribute.setColumnType(rs.getString("column_type"));
                metaAttribute.setMetaType(new MetaValue(MetaDataType.valueOf(rs.getString("type_code"))));
                metaAttribute.setKey(rs.getBoolean("is_key"));
                metaAttribute.setKeySet(Converter.convertToShort(rs.getShort("key_code")));
                metaAttribute.setNullable(rs.getBoolean("is_nullable"));
                metaAttribute.setFinal(rs.getBoolean("is_final"));
                metaAttribute.setImmutable(rs.getBoolean("is_immutable"));

                MetaSet metaSet = new MetaSet(new MetaValue(MetaDataType.valueOf(rs.getString("type_code"))));
                metaSet.setId(Converter.convertToLong(rs.getLong("id")));

                //TODO: добавить так как в поле ARRAY_KEY_TYPE есть необходимость
                //metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

                metaAttribute.setMetaType(metaSet);

                return metaAttribute;
            });

        for (MetaAttribute row : attributes)
            metaClass.setMetaAttribute(row.getName(), row);
    }

    private void loadComplexAttributes(MetaClass metaClass) {
        List<MetaAttribute> attributes = npJdbcTemplate.query("select * from EAV_CORE.EAV_M_COMPLEX_ATTRIBUTES where CLASS_ID = :classId",
            new MapSqlParameterSource("classId", metaClass.getId()), (rs, i) -> {
                MetaAttribute metaAttribute = new MetaAttribute(rs.getLong("id"));

                metaAttribute.setTitle(rs.getString("title"));
                metaAttribute.setName(rs.getString("name"));
                metaAttribute.setColumnName(rs.getString("column_name"));
                metaAttribute.setColumnType(rs.getString("column_type"));
                metaAttribute.setMetaType(load(rs.getLong("ref_class_id")));
                metaAttribute.setKey(rs.getBoolean("is_key"));
                metaAttribute.setNullable(rs.getBoolean("is_nullable"));
                metaAttribute.setFinal(rs.getBoolean("is_final"));
                metaAttribute.setImmutable(rs.getBoolean("is_immutable"));

                return metaAttribute;
            });

        for (MetaAttribute row : attributes)
            metaClass.setMetaAttribute(row.getName(), row);
    }

    private void loadComplexArrays(MetaClass metaClass) {
        List<MetaAttribute> attributes = npJdbcTemplate.query("select * from EAV_CORE.EAV_M_COMPLEX_SET where CLASS_ID = :classId",
            new MapSqlParameterSource("classId", metaClass.getId()), (rs, i) -> {
                MetaAttribute metaAttribute = new MetaAttribute(rs.getLong("id"));

                metaAttribute.setName(rs.getString("name"));
                metaAttribute.setTitle(rs.getString("title"));
                metaAttribute.setColumnName(rs.getString("column_name"));
                metaAttribute.setColumnType(rs.getString("column_type"));
                metaAttribute.setKey(rs.getBoolean("is_key"));
                metaAttribute.setKeySet(Converter.convertToShort(rs.getShort("key_code")));
                metaAttribute.setNullable(rs.getBoolean("is_nullable"));
                metaAttribute.setFinal(rs.getBoolean("is_final"));
                metaAttribute.setImmutable(rs.getBoolean("is_immutable"));

                MetaSet metaSet = new MetaSet(load(rs.getLong("ref_class_id")));
                metaSet.setId(Converter.convertToLong(rs.getLong("id")));

                //TODO: добавить так как в поле ARRAY_KEY_TYPE есть необходимость
                //metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

                metaAttribute.setMetaType(metaSet);

                return metaAttribute;
            });

        for (MetaAttribute row : attributes)
            metaClass.setMetaAttribute(row.getName(), row);
    }

    private class MetaClassMapper implements RowMapper<MetaClass> {
        private final MetaClass metaClass;

        MetaClassMapper() {
            metaClass = new MetaClass();
        }

        private MetaClassMapper(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        public MetaClass mapRow(ResultSet rs, int rowNum) throws SQLException {
            metaClass.setId(rs.getLong("id"));
            metaClass.setBeginDate(Converter.convertToLocalDate(rs.getDate("report_date")));
            metaClass.setClassName(rs.getString("name"));
            metaClass.setClassTitle(rs.getString("title"));
            metaClass.setSchemaXml(rs.getString("schema_xml"));
            metaClass.setSchemaData(rs.getString("schema_data"));
            metaClass.setTableName(rs.getString("table_name"));
            metaClass.setDictionary(rs.getByte("is_dictionary") == 1);
            metaClass.setParentIsKey(rs.getByte("parent_is_key") == 1);

            //TODO: добавить так как в поле COMPLEX_KEY_TYPE есть необходимость
            //metaClass.setComplexKeyType(ComplexKeyTypes.valueOf((String) row.get("complex_key_type")));

            return metaClass;
        }
    }

}
