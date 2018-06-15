package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.meta.MetaClass;

import java.time.LocalDate;

public interface BaseEntityLoadService {

    BaseEntity loadBaseEntity(Long id, Long respondentId, MetaClass metaClass, LocalDate existingReportDate, LocalDate savingReportDate);

}
