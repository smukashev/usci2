package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.dao.EavHubDao;
import kz.bsbnb.usci.core.model.EavHub;
import kz.bsbnb.usci.core.service.EavHubService;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.Persistable;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseValue;
import kz.bsbnb.usci.model.eav.meta.*;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author BSB
 */

@Service
public class EavHubServiceImpl implements EavHubService {
    private DateTimeFormatter HUB_KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final EavHubDao eavHubDao;

    public EavHubServiceImpl(EavHubDao eavHubDao) {
        this.eavHubDao = eavHubDao;
    }

    @Override
    //TODO: добавить поддержку parentIsKey
    public String getKeyString(BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMetaClass();
        if (metaClass == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E176));

        //TODO: добавить ошибку в Errors
        if (!metaClass.isSearchable())
            throw new IllegalArgumentException("Метод только обрабатывает мета классы которые содержат ключевые поля");

        StringBuilder sb = new StringBuilder();

        List<MetaAttribute> keyAttributes = baseEntity.getAttributes().stream()
                .map(baseEntity::getMetaAttribute)
                .filter(MetaAttribute::isKey)
                .sorted((o1, o2) -> (int)(o2.getId() - o1.getId()))
                .collect(Collectors.toList());

        for (MetaAttribute metaAttribute : keyAttributes) {
            MetaType metaType = metaAttribute.getMetaType();

            if (!metaAttribute.isKey())
                continue;

            BaseValue baseValue = baseEntity.getBaseValue(metaAttribute.getName());
            if (baseValue == null || baseValue.getValue() == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E188));

            //TODO: пока не известно нужны ли сеты
            //TODO: добавить ошибку в Errors
            if (metaType.isSet())
                throw new IllegalArgumentException("Сеты в качестве ключевых полей в EAV_HUB не поддерживатюся");

            String key = null;

            if (metaType.isComplex()) {
                BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();

                //TODO: добавить ошибку в Errors
                if (childBaseEntity.getId() == 0)
                    throw new IllegalArgumentException("Ключевому поле сущности не присвоено id");

                key = String.valueOf(childBaseEntity.getId());
            }
            else {
                MetaValue metaValue = (MetaValue)metaType;
                MetaDataType metaDataType = metaValue.getMetaDataType();
                if (metaDataType == MetaDataType.DOUBLE || metaDataType == MetaDataType.INTEGER || metaDataType == MetaDataType.BOOLEAN)
                    throw new IllegalArgumentException(String.format("Типы данных %s не могут участвовать в ключе EAV_HUB", metaValue.getMetaDataType()));
                else if (metaDataType == MetaDataType.DATE) {
                    LocalDate date = (LocalDate) baseValue.getValue();
                    key = date.format(HUB_KEY_DATE_FORMAT);
                }
                else if (metaDataType == MetaDataType.STRING)
                    key = (String)baseValue.getValue();
            }


            if (sb.length() > 0)
                sb.append(Errors.SEPARATOR);

            sb.append(key);
        }

        return sb.toString();
    }

    @Override
    public Long insert(EavHub eavHub) {
        return eavHubDao.insert(eavHub);
    }

    @Override
    public Long find(BaseEntity baseEntity) {
        //TODO: предусмотреть отправку parentEntityId
        return eavHubDao.find(baseEntity.getRespondentId(), baseEntity.getMetaClass().getId(), getKeyString(baseEntity), null);
    }

    @Override
    public EavHub find(Long entityId) {
        return eavHubDao.find(entityId);
    }

    @Override
    public void delete(Long entityId) {
        eavHubDao.delete(entityId);
    }

    @Override
    public void update(EavHub eavHub) {
        eavHubDao.update(eavHub);
    }

}
