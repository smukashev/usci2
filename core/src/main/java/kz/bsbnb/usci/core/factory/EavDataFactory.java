package kz.bsbnb.usci.core.factory;

import kz.bsbnb.usci.core.repository.MetaClassRepository;
import kz.bsbnb.usci.model.eav.data.BaseEntity;
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

    public BaseEntity createBaseEntity(String metaClassName) {
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setMetaClass(metaClassRepository.getMetaClass(metaClassName));
        return baseEntity;
    }



}
