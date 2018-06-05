package trash;

/**
 * @author BSB
 */

public interface EavHubDao {

    long insert(EavHub eavHub);

    long find(Long respondentId, Long metaClassId, String entityKey);

    EavHub find(Long entityId);

    void delete(Long entityId);

    void update(EavHub eavHub);

}
