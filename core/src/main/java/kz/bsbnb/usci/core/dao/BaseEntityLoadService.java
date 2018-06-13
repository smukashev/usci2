package kz.bsbnb.usci.core.dao;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

import java.time.LocalDate;

public interface BaseEntityLoadService {

    BaseEntity loadBaseEntity(BaseEntity baseEntity, LocalDate reportDate);

}
