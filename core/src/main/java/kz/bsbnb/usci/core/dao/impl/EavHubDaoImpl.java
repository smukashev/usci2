package kz.bsbnb.usci.core.dao.impl;

import kz.bsbnb.usci.core.dao.EavHubDao;
import kz.bsbnb.usci.core.model.EavHub;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.util.Converter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

/**
 * @author BSB
 */

@Repository
public class EavHubDaoImpl implements EavHubDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate npJdbcTemplate;

    public EavHubDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate npJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Override
    public Long insert(EavHub eavHub) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName("EAV_DATA")
                .withTableName("EAV_HUB")
                .usingGeneratedKeyColumns("ENTITY_ID");

        Number id = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("CREDITOR_ID", eavHub.getRespondentId())
                .addValue("BATCH_ID", eavHub.getBatchId())
                .addValue("ENTITY_KEY", eavHub.getEntityKey())
                .addValue("CLASS_ID", eavHub.getMetaClassId()));

        eavHub.setEntityId(id.longValue());

        return id.longValue();
    }

    @Override
    public Long find(Long respondentId, Long metaClassId, String entityKey, Long parentEntityId) {
        try {
            String query = "select ENTITY_ID\n" +
                    "  from EAV_DATA.EAV_HUB\n" +
                    " where CREDITOR_ID = :respondentId\n" +
                    "   and ENTITY_KEY = :entityKey\n" +
                    "   and CLASS_ID = :metaClassId\n";

            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("respondentId", respondentId)
                .addValue("metaClassId", metaClassId)
                .addValue("entityKey", entityKey);

            if (parentEntityId != null) {
                query += " and PARENT_ENTITY_ID = :parentEntityId\n";
                params.addValue("parentEntityId", parentEntityId);
            }

            return npJdbcTemplate.queryForObject(query, params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new IllegalArgumentException(Errors.compose(Errors.E91, entityKey));
        }
    }

    @Override
    public EavHub find(Long entityId) {
        return npJdbcTemplate.queryForObject("select * from EAV_DATA.EAV_HUB where ENTITY_ID = :entityId",
                new MapSqlParameterSource("entityId", entityId),
                (rs, rowNum) -> new EavHub(Converter.convertToLong(rs.getObject("respondent_id")),
                        rs.getString("entity_key"),
                        Converter.convertToLong(rs.getObject("class_id")),
                        rs.getLong("entity_id"),
                        Converter.convertToLong(rs.getObject("batch_id"))));
    }

    @Override
    public void delete(Long entityId) {
        int count = npJdbcTemplate.update("delete from EAV_DATA.EAV_HUB where ENTITY_ID = :entityId",
                new MapSqlParameterSource("entityId", entityId));
        if (count == 0)
            throw new IllegalArgumentException("Ошибка delete из таблицы EAV_DATA.EAV_HUB");
    }

    @Override
    public void update(EavHub eavHub) {
        int count = npJdbcTemplate.update("UPDATE EAV_DATA.EAV_HUB\n" +
                        "   SET ENTITY_KEY = :entityKey, BATCH_ID = :batchId\n" +
                        " WHERE ENTITY_ID = :entityId",
                new MapSqlParameterSource("entityId", eavHub.getEntityId())
                    .addValue("entityKey", eavHub.getEntityKey()));
        if (count == 0)
            throw new IllegalArgumentException("Ошибка update записи в таблице EAV_DATA.EAV_HUB");
    }

}
