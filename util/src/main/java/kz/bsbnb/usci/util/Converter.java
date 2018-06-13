package kz.bsbnb.usci.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Converter {
    public static Timestamp convertToSqlTimestamp(LocalDateTime date) {
        return date == null? null: Timestamp.valueOf(date);
    }

    public static java.sql.Date convertToSqlDate(LocalDate date) {
        return date == null? null: java.sql.Date.valueOf(date);
    }

    public static java.sql.Date convertToSqlDate(java.util.Date date) {
        return date == null? null: new java.sql.Date(date.getTime());
    }

    public static Long convertToLong(Object value) {
        return value == null? null: (value instanceof BigDecimal)? ((BigDecimal)value).longValue() : (Long)value;
    }

    public static Integer convertToInt(Object value) {
        return value == null? null: (value instanceof BigDecimal)? ((BigDecimal)value).intValue() : (Integer)value;
    }

    public static Short convertToShort(Object value) {
        return value == null? null: (value instanceof BigDecimal)? ((BigDecimal)value).shortValue() : (Short)value;
    }

    public static Double convertToDouble(Object value) {
        return value == null? null: (value instanceof BigDecimal)? ((BigDecimal)value).doubleValue() : (Double)value;
    }

    public static LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
        return timestamp == null? null: timestamp.toLocalDateTime();
    }

    public static LocalDate convertToLocalDate(java.sql.Date date) {
        return date == null? null: date.toLocalDate();
    }

    public static LocalDate convertToLocalDate(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate convertToLocalDate(java.sql.Timestamp date) {
        return date == null? null: date.toLocalDateTime().toLocalDate();
    }

}
