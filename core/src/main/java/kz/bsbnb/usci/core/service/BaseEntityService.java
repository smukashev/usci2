package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

import java.time.LocalDate;

public interface BaseEntityService {

    boolean existsBaseEntity(BaseEntity baseEntity, LocalDate reportDate);

    LocalDate getMaxReportDate(BaseEntity baseEntity, LocalDate reportDate);

    LocalDate getMinReportDate(BaseEntity baseEntity, LocalDate reportDate);

}
