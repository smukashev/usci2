package kz.bsbnb.usci.core.factory;

import kz.bsbnb.usci.core.repository.MetaClassRepository;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseSet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class EavBaseFactory {
    private final MetaClassRepository metaClassRepository;

    public EavBaseFactory(MetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }

    public BaseEntity createBaseEntity(String metaClassName, LocalDate reportDate, Long respondentId, Long batchId) {
        Objects.requireNonNull(metaClassName);
        Objects.requireNonNull(respondentId);
        Objects.requireNonNull(reportDate);
        Objects.requireNonNull(batchId);

        return new BaseEntity(metaClassRepository.getMetaClass(metaClassName), respondentId, reportDate, batchId);
    }

    public BaseSet createBaseSet(String metaClassName) {
        Objects.requireNonNull(metaClassName);

        return new BaseSet(metaClassRepository.getMetaClass(metaClassName));
    }

}
