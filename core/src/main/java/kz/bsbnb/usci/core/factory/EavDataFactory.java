package kz.bsbnb.usci.core.factory;

import kz.bsbnb.usci.core.repository.MetaClassRepository;
import kz.bsbnb.usci.model.eav.data.EavDataEntity;
import org.springframework.stereotype.Component;

/**
 * @author BSB
 */

@Component
public class EavDataFactory {
    private final MetaClassRepository metaClassRepository;

    public EavDataFactory(MetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }

    public EavDataEntity createDataEntity(String metaClassName) {
        EavDataEntity eavDataEntity = new EavDataEntity();
        eavDataEntity.setMetaClass(metaClassRepository.getMetaClass(metaClassName));
        return eavDataEntity;
    }

}
