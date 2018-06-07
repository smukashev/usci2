package kz.bsbnb.usci.core.dao.impl;

import kz.bsbnb.usci.core.dao.BaseEntityDao;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author BSB
 */

public class BaseEntityDaoImpl implements BaseEntityDao {
    private final JdbcTemplate jdbcTemplate;

    public BaseEntityDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long insert(BaseEntity baseEntity) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName("EAV_DATA")
                .withTableName("EAV_BE_ENTITIES")
                .usingGeneratedKeyColumns("ID");

        Number id = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource("class_id", baseEntity.getMetaClass().getId()));

        baseEntity.setId(id.longValue());

        return id.longValue();
    }

}
