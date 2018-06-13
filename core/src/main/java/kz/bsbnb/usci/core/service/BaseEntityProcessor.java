package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

import java.sql.SQLException;
import java.time.LocalDate;

public interface BaseEntityProcessor {

    BaseEntity processBaseEntity(BaseEntity saving, LocalDate reportDate) throws SQLException;

    BaseEntity prepareBaseEntity(BaseEntity baseEntity);

}
