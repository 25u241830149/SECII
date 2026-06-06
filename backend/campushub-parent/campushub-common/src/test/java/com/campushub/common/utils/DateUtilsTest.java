package com.campushub.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void nowReturnsShanghaiTime() {
        LocalDateTime now = DateUtils.now();

        assertNotNull(now);
        assertEquals(ZoneId.of("Asia/Shanghai"), DateUtils.defaultZone());
    }

    @Test
    void formatDateReturnsExpectedPattern() {
        LocalDate date = LocalDate.of(2026, 6, 5);

        assertEquals("2026-06-05", DateUtils.formatDate(date));
    }

    @Test
    void formatDateReturnsNullForNull() {
        assertNull(DateUtils.formatDate(null));
    }

    @Test
    void formatDateTimeReturnsExpectedPattern() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 5, 15, 30, 0);

        assertEquals("2026-06-05 15:30:00", DateUtils.formatDateTime(dateTime));
    }

    @Test
    void formatDateTimeReturnsNullForNull() {
        assertNull(DateUtils.formatDateTime(null));
    }

    @Test
    void parseDateParsesValidString() {
        assertEquals(LocalDate.of(2026, 6, 5), DateUtils.parseDate("2026-06-05"));
    }

    @Test
    void parseDateReturnsNullForNullOrBlank() {
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
        assertNull(DateUtils.parseDate("   "));
    }

    @Test
    void parseDateTimeParsesValidString() {
        assertEquals(LocalDateTime.of(2026, 6, 5, 15, 30, 0),
                DateUtils.parseDateTime("2026-06-05 15:30:00"));
    }

    @Test
    void parseDateTimeReturnsNullForNullOrBlank() {
        assertNull(DateUtils.parseDateTime(null));
        assertNull(DateUtils.parseDateTime("   "));
    }

    @Test
    void isValidDateAcceptsCorrectPattern() {
        assertTrue(DateUtils.isValidDate("2026-06-05"));
        assertFalse(DateUtils.isValidDate("2026/06/05"));
        assertFalse(DateUtils.isValidDate(null));
        assertFalse(DateUtils.isValidDate(""));
    }

    @Test
    void isValidDateTimeAcceptsCorrectPattern() {
        assertTrue(DateUtils.isValidDateTime("2026-06-05 15:30:00"));
        assertFalse(DateUtils.isValidDateTime("2026-06-05T15:30:00"));
        assertFalse(DateUtils.isValidDateTime(null));
    }

    @Test
    void toInstantAndFromInstantAreReversible() {
        LocalDateTime original = LocalDateTime.of(2026, 6, 5, 12, 0, 0);
        Instant instant = DateUtils.toInstant(original);

        assertNotNull(instant);
        assertEquals(original, DateUtils.fromInstant(instant));
    }

    @Test
    void toInstantReturnsNullForNull() {
        assertNull(DateUtils.toInstant(null));
    }

    @Test
    void fromInstantReturnsNullForNull() {
        assertNull(DateUtils.fromInstant(null));
    }
}
