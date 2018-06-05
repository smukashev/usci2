package trash;

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
    public long insert(EavHub eavHub) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName("EAV_DATA")
                .withTableName("EAV_BE_ENTITIES")
                .usingGeneratedKeyColumns("ENTITY_ID");

        Number id = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("CREDITOR_ID", eavHub.getRespondentId())
                .addValue("ENTITY_ID", eavHub.getEntityId())
                .addValue("ENTITY_KEY", eavHub.getEntityKey())
                .addValue("CLASS_ID", eavHub.getMetaClassId())
                .addValue("IS_DELETED", eavHub.getDeleted())
                .addValue("SYSTEM_DATE", eavHub.getSystemDate()));

        eavHub.setEntityId(id.longValue());

        return id.longValue();
    }

    @Override
    public long find(Long respondentId, Long metaClassId, String entityKey) {
        try {
            return npJdbcTemplate.queryForObject("select ENTITY_ID\n" +
                            "  from EAV_DATA.EAV_BE_ENTITIES\n" +
                            " where CREDITOR_ID = :respondentId\n" +
                            "   and ENTITY_KEY = :entityKey" +
                            "   and CLASS_ID = :metaClassId",
                    new MapSqlParameterSource()
                            .addValue("respondentId", respondentId)
                            .addValue("metaClassId", metaClassId)
                            .addValue("entityKey", entityKey), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new IllegalArgumentException(Errors.compose(Errors.E91, entityKey));
        }
    }

    @Override
    public EavHub find(Long entityId) {
        return npJdbcTemplate.queryForObject("select * from EAV_DATA.EAV_BE_ENTITIES where ENTITY_ID = :entityId",
                new MapSqlParameterSource("entityId", entityId),
                (rs, rowNum) -> {
                    EavHub tempEavHub = new EavHub();
                    tempEavHub.setEntityId(rs.getLong(""));
                    tempEavHub.setDeleted(rs.getByte("IS_DELETED") == 1);
                    tempEavHub.setEntityKey(rs.getString("ENTITY_KEY"));
                    tempEavHub.setMetaClassId(Converter.convertToLong(rs.getObject("")));
                    tempEavHub.setSystemDate(Converter.convertToLocalDateTime(rs.getTimestamp("SYSTEM_DATE")));
                    return tempEavHub;
                });
    }

    @Override
    public void delete(Long entityId) {
        int count = npJdbcTemplate.update("delete from EAV_DATA.EAV_BE_ENTITIES where ENTITY_ID = :entityId",
                new MapSqlParameterSource("entityId", entityId));
        if (count == 0)
            throw new IllegalArgumentException("Ошибка delete из таблицы EAV_DATA.EAV_BE_ENTITIES");
    }

    @Override
    public void update(EavHub eavHub) {
        int count = npJdbcTemplate.update("update EAV_DATA.EAV_BE_ENTITIES set ENTITY_KEY = :entityKey where ENTITY_ID = :entityId",
                new MapSqlParameterSource("entityId", eavHub.getEntityId())
                    .addValue("entityKey", eavHub.getEntityKey()));
        if (count == 0)
            throw new IllegalArgumentException("Ошибка update записи в таблице EAV_DATA.EAV_BE_ENTITIES");
    }

}
