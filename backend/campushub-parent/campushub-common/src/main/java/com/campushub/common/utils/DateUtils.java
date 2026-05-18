package com.campushub.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    private DateUtils() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? null : DATE_FORMATTER.format(date);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATETIME_FORMATTER.format(dateTime);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, DATETIME_FORMATTER);
    }

    public static boolean isValidDate(String value) {
        return canParse(value, true);
    }

    public static boolean isValidDateTime(String value) {
        return canParse(value, false);
    }

    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    public static LocalDateTime fromInstant(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
    }

    public static ZoneId defaultZone() {
        return DEFAULT_ZONE;
    }

    private static boolean canParse(String value, boolean dateOnly) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            if (dateOnly) {
                parseDate(value);
            } else {
                parseDateTime(value);
            }
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
