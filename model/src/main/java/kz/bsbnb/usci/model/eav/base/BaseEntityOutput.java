package kz.bsbnb.usci.model.eav.base;

import kz.bsbnb.usci.model.eav.meta.MetaAttribute;
import kz.bsbnb.usci.model.eav.meta.MetaClass;
import kz.bsbnb.usci.model.eav.meta.MetaSet;
import kz.bsbnb.usci.model.eav.meta.MetaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Artur Tkachenko
 * @author Alexandr Motov
 * @author Kanat Tulbassiev
 * @author Baurzhan Makhambetov
 */

public class BaseEntityOutput {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityOutput.class);

    public static String toString(BaseEntity entity) {
        return toString(entity, "");
    }

    public static String toString(BaseEntity entity, String prefix) {
        if (entity == null) return "null";

        String str = entity.getMetaClass().getClassName() + "(" + entity.getId() + ", ";
        try {
            str += entity.getReportDate() == null ? "-)" : formatDate(entity.getReportDate()) + ");";
        } catch (Exception e) {
            if (entity.getMetaClass().getClassName().equals("credit")) {
                logger.info(String.valueOf(entity.getEl("primary_contract.no")));
                logger.info(String.valueOf(entity.getEl("primary_contract.date")));
            }
            logger.info(String.valueOf(entity));
            logger.info(String.valueOf(entity.getReportDate()));
            throw e;
        }

        MetaClass meta = entity.getMetaClass();

        for (String memberName : meta.getAttributeNames()) {
            MetaAttribute attribute = meta.getMetaAttribute(memberName);
            MetaType type = attribute.getMetaType();

            //TODO:
            //if (meta.isDictionary() && !(attribute.isKey() || attribute.isOptionalKey()))
            //    continue;

            BaseValue value = entity.getBaseValue(memberName);

            String valueToString = "null";
            boolean valueIsNull = false;

            if (value == null) {
                valueToString = "not set";
                valueIsNull = true;
            } else {
                if (value.getValue() == null) {
                    valueToString = "null";
                }
            }

            if (value != null && value.getValue() != null) {
                if (type.isComplex()) {
                    if (!type.isSet()) {
                        valueToString = toString((BaseEntity) value.getValue(), prefix + "\t");
                    } else {
                        valueToString = complexSet((BaseSet) value.getValue(), prefix + "\t", (MetaSet) type);
                    }
                } else {
                    valueToString = value.getValue().toString();
                }
            }

            //TODO:
            /*if (!valueIsNull) {
                if (attribute.isKey() || attribute.isOptionalKey()) {
                    str += "\n" + prefix +  memberName  + " : ";
                } else {
                    str += "\n" + prefix + memberName + " : ";
                }

                if (type.isSet())
                    str += value.getId() + " : ";

                str += DataTypes.formatDate(value.getRepDate()) + " : " + valueToString;
            }*/
        }

        return str;
    }

    private static String complexSet(BaseSet set, String prefix, MetaSet metaSet) {
        String str = "";

        for (BaseValue value : set.getValues()) {
            if (metaSet.isSet()) {
                if (metaSet.isComplex()) {
                    str += "\n" + prefix + toString((BaseEntity) value.getValue(), prefix + "\t");
                } else {
                    str += value.getValue().toString();
                }
            }
        }

        return str;
    }

    public synchronized static String formatDate(LocalDate d) {
        return  d.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

}
