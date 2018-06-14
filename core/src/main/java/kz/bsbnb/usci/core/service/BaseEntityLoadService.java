package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

import java.time.LocalDate;

public interface BaseEntityLoadService {

    BaseEntity loadBaseEntity(BaseEntity baseEntity, LocalDate existingReportDate, LocalDate savingReportDate);

}
