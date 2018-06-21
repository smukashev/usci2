package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.dao.EavHubDao;
import kz.bsbnb.usci.core.model.EavHub;
import kz.bsbnb.usci.core.service.EavHubService;
import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.eav.base.BaseEntity;
import kz.bsbnb.usci.model.eav.base.BaseValue;
import kz.bsbnb.usci.model.eav.meta.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author BSB
 */

@Service
public class EavHubServiceImpl implements EavHubService {
    private final static char KEY_DELIMITER = '~';
    private DateTimeFormatter HUB_KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final EavHubDao eavHubDao;

    public EavHubServiceImpl(EavHubDao eavHubDao) {
        this.eavHubDao = eavHubDao;
    }

    @Override
    //TODO: добавить поддержку parentIsKey
    public String getKeyString(BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMetaClass();

        //TODO: добавить ошибку в Errors
        if (!metaClass.isSearchable())
            throw new IllegalArgumentException("Метод только обрабатывает мета классы которые содержат ключевые поля");

        StringBuilder sb = new StringBuilder();

        // получаем все ключевые атрибуты сущности
        metaClass.getAttributes().stream()
            .filter(MetaAttribute::isKey)
            .sorted((o1, o2) -> (int)(o2.getId() - o1.getId()))
            .forEach(metaAttribute -> {
                MetaType metaType = metaAttribute.getMetaType();

                BaseValue baseValue = baseEntity.getBaseValue(metaAttribute.getName());
                if (baseValue == null || baseValue.getValue() == null)
                    throw new IllegalArgumentException(Errors.compose(Errors.E188));

                if (metaType.isSet())
                    throw new IllegalArgumentException("Сеты в качестве ключевых полей в EAV_HUB не поддерживатюся");

                String key = null;

                if (metaType.isComplex()) {
                    BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();

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
                    sb.append(KEY_DELIMITER);

                sb.append(key);
            });

        return sb.toString();
    }

    @Override
    public void insert(EavHub eavHub) {
        if (eavHub.getEntityId() != null)
            throw new IllegalArgumentException("id сущности для инсерта в EAV_HUB должна быть пустой");

        eavHubDao.insert(eavHub);
    }

    @Override
    public void insert(BaseEntity baseEntity) {
        eavHubDao.insert(new EavHub(baseEntity.getId(), baseEntity.getRespondentId(), getKeyString(baseEntity),
                baseEntity.getMetaClass().getId(), baseEntity.getBatchId()));
    }

    @Override
    public Long find(BaseEntity baseEntity) {
        Optional<MetaAttribute> nullComplexKey = baseEntity.getAttributes()
            .filter(MetaAttribute::isKey)
            .filter(metaAttribute -> {
                MetaType metaType = metaAttribute.getMetaType();
                if (metaType.isComplex()) {
                    BaseValue baseValue = baseEntity.getBaseValue(metaAttribute.getName());
                    BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();
                    return metaType.isComplex() && childBaseEntity.getId() == null;
                }
                return false;
            }).findAny();

        if (nullComplexKey.isPresent())
            return null;

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
