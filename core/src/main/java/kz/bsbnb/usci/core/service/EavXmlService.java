package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.model.eav.base.BaseEntity;

import java.sql.SQLException;

public interface EavXmlService {

    BaseEntity process(BaseEntity saving) throws SQLException;

}
