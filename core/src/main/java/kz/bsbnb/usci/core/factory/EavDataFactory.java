package kz.bsbnb.usci.core.factory;

import kz.bsbnb.usci.core.repository.MetaClassRepository;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author BSB
 */

@Component
public class EavDataFactory {
    private final MetaClassRepository metaClassRepository;

    public EavDataFactory(MetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }

    //это пока черновой вариант
    public BaseEntity createBaseEntity(String metaClassName, LocalDate reportDate, Long respondentId, Long batchId) {
        if (metaClassName == null)
            throw new IllegalArgumentException("");

        return new BaseEntity(metaClassRepository.getMetaClass(metaClassName), respondentId, reportDate, batchId);
    }

    //это пока черновой вариант
    public BaseSet createBaseSet(String metaClassName) {
        return new BaseSet(metaClassRepository.getMetaClass(metaClassName));
    }

}
