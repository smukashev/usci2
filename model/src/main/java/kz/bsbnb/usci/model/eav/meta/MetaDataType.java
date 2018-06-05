package kz.bsbnb.usci.model.eav.meta;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.util.Converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public enum MetaDataType {
    INTEGER,
    DATE,
    STRING,
    BOOLEAN,
    DOUBLE;

    public static final String DATE_FORMAT_SLASH = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DOT = "dd.MM.yyyy";

    private static final DateFormat dateFormatSlash = new SimpleDateFormat(DATE_FORMAT_SLASH);
    private static final DateFormat dateFormatDot = new SimpleDateFormat(DATE_FORMAT_DOT);

    // todo: new instance of date format
    public synchronized static Date parseDate(String s) throws ParseException {
        try {
            return dateFormatDot.parse(s);
        } catch (ParseException e) {
            return dateFormatSlash.parse(s);
        }
    }

    public synchronized static Date parseSplashDate(String s) throws ParseException {
        return dateFormatSlash.parse(s);
    }

    public synchronized static String formatDate(Date d) {
        return dateFormatDot.format(d);
    }

    public static Class<?> getDataTypeClass(MetaDataType dataType) {
        switch (dataType) {
            case INTEGER:
                return Integer.class;
            case DATE:
                return LocalDate.class;
            case STRING:
                return String.class;
            case BOOLEAN:
                return Boolean.class;
            case DOUBLE:
                return Double.class;
            default:
                throw new IllegalArgumentException(Errors.compose(Errors.E49));
        }
    }

    // конвертация переменной примитивного типа в тип реляционной модели
    // Boolean значения конвертируются в Varchar2(1)
    // LocalDate конвертируется в Sql.Date
    // остальные значения остаются как есть
    public static Object convertToRmValue(MetaDataType metaDataType, Object value) {
        if (metaDataType == MetaDataType.DATE)
            return Converter.convertToSqlDate((LocalDate)value);
        else if (metaDataType == MetaDataType.BOOLEAN)
            return String.valueOf(((Boolean)(value))? 1: 0);
        else
            return value;
    }

    public Class<?> getDataTypeClass() {
        return getDataTypeClass(this);
    }
}
