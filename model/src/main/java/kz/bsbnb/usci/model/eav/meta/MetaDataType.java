package kz.bsbnb.usci.model.eav.meta;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
                return Date.class;
            case STRING:
                return String.class;
            case BOOLEAN:
                return Boolean.class;
            case DOUBLE:
                return Double.class;
            // TODO
//            default:
//                throw new IllegalArgumentException(Errors.compose(Errors.E49));
        }
        return null;
    }

    public static Object getCastObject(MetaDataType typeCode, String value) {
        switch(typeCode) {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                Date date = null;

                try {
                    synchronized (MetaDataType.class) {
                        date = dateFormatSlash.parse(value);
                    }
                } catch (ParseException e) {
                    try {
                        synchronized (MetaDataType.class) {
                            date = dateFormatDot.parse(value);
                        }
                    } catch (ParseException ex) {
                        e.printStackTrace();
                    }
                }

                return date;
            case STRING:
                return value;
            case BOOLEAN:
                try {
                    int i = Integer.parseInt(value);
                    return i == 1;
                } catch (Exception e) {
                    return Boolean.parseBoolean(value);
                }
            case DOUBLE:
                return Double.parseDouble(value);
            // TODO
//            default:
//                throw new IllegalArgumentException(Errors.compose(Errors.E127));
        }
        return null;
    }

    public Class<?> getDataTypeClass() {
        return getDataTypeClass(this);
    }
}
