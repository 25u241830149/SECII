package com.campushub.common.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campushub.common.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidateUtilsTest {

    // ---------- isBlank / isNotBlank ----------

    @Test
    void isBlankReturnsTrueForNullOrEmptyOrWhitespace() {
        assertTrue(ValidateUtils.isBlank(null));
        assertTrue(ValidateUtils.isBlank(""));
        assertTrue(ValidateUtils.isBlank("   "));
    }

    @Test
    void isBlankReturnsFalseForNonBlank() {
        assertFalse(ValidateUtils.isBlank("hello"));
        assertFalse(ValidateUtils.isBlank(" a "));
    }

    @Test
    void isEmptyReturnsTrueForNullOrEmptyCollection() {
        assertTrue(ValidateUtils.isEmpty(null));
        assertTrue(ValidateUtils.isEmpty(Collections.emptyList()));
    }

    @Test
    void isEmptyReturnsFalseForNonEmptyCollection() {
        assertFalse(ValidateUtils.isEmpty(List.of("a")));
    }

    // ---------- isUsername ----------

    @Test
    void isValidUsernameMatchesFormat() {
        assertTrue(ValidateUtils.isUsername("alice2026"));
        assertTrue(ValidateUtils.isUsername("bob_1234"));
    }

    @Test
    void isUsernameRejectsNullShortLongOrSpecialChars() {
        assertFalse(ValidateUtils.isUsername(null));
        assertFalse(ValidateUtils.isUsername("ab"));          // too short (< 4)
        assertFalse(ValidateUtils.isUsername("a".repeat(21))); // too long (> 20)
        assertFalse(ValidateUtils.isUsername("hello world")); // space
    }

    // ---------- isPassword ----------

    @Test
    void isValidPasswordMatchesFormat() {
        assertTrue(ValidateUtils.isPassword("Pass12345"));
        assertTrue(ValidateUtils.isPassword("12345678"));
    }

    @Test
    void isPasswordRejectsNullShortLongOrWhitespace() {
        assertFalse(ValidateUtils.isPassword(null));
        assertFalse(ValidateUtils.isPassword("short  "));    // < 8 (after trim would be 5, but pattern rejects space)
        assertFalse(ValidateUtils.isPassword("abc defgh"));  // contains space
        assertFalse(ValidateUtils.isPassword("a".repeat(21)));
    }

    // ---------- isEmail ----------

    @Test
    void isValidEmailMatchesFormat() {
        assertTrue(ValidateUtils.isEmail("a@b.cn"));
        assertTrue(ValidateUtils.isEmail("alice@example.com"));
        assertTrue(ValidateUtils.isEmail("test.user@mail.campushub.cn"));
    }

    @Test
    void isEmailRejectsNullAndInvalidFormats() {
        assertFalse(ValidateUtils.isEmail(null));
        assertFalse(ValidateUtils.isEmail(""));
        assertFalse(ValidateUtils.isEmail("no-at-sign"));
        assertFalse(ValidateUtils.isEmail("@missing-local.com"));
    }

    // ---------- isPhone ----------

    @Test
    void isValidPhoneMatchesChineseMobilePattern() {
        assertTrue(ValidateUtils.isPhone("13800000000"));
        assertTrue(ValidateUtils.isPhone("19912345678"));
    }

    @Test
    void isPhoneRejectsNullAndInvalidFormats() {
        assertFalse(ValidateUtils.isPhone(null));
        assertFalse(ValidateUtils.isPhone("12345678901")); // not starting with 1[3-9]
        assertFalse(ValidateUtils.isPhone("1380000000"));  // too short
        assertFalse(ValidateUtils.isPhone("13800000000a")); // contains letter
    }

    // ---------- isStudentId ----------

    @Test
    void isValidStudentIdMatchesFormat() {
        assertTrue(ValidateUtils.isStudentId("20260001"));
        assertTrue(ValidateUtils.isStudentId("2024110101"));
    }

    @Test
    void isStudentIdRejectsNullAndNonDigit() {
        assertFalse(ValidateUtils.isStudentId(null));
        assertFalse(ValidateUtils.isStudentId("abc"));
        assertFalse(ValidateUtils.isStudentId("123"));  // too short (< 4)
    }

    // ---------- isPositive ----------

    @Test
    void isPositiveRejectsNullOrNonPositive() {
        assertFalse(ValidateUtils.isPositive((Long) null));
        assertFalse(ValidateUtils.isPositive(0L));
        assertFalse(ValidateUtils.isPositive(-1L));

        assertFalse(ValidateUtils.isPositive((Integer) null));
        assertFalse(ValidateUtils.isPositive(0));
        assertFalse(ValidateUtils.isPositive(-1));

        assertFalse(ValidateUtils.isPositive((BigDecimal) null));
        assertFalse(ValidateUtils.isPositive(BigDecimal.ZERO));
        assertFalse(ValidateUtils.isPositive(BigDecimal.valueOf(-0.01)));
    }

    @Test
    void isPositiveReturnsTrueForPositiveValues() {
        assertTrue(ValidateUtils.isPositive(1L));
        assertTrue(ValidateUtils.isPositive(1));
        assertTrue(ValidateUtils.isPositive(BigDecimal.valueOf(0.01)));
    }

    // ---------- isUrl ----------

    @Test
    void isValidUrlAcceptsHttpAndHttps() {
        assertTrue(ValidateUtils.isUrl("http://example.com"));
        assertTrue(ValidateUtils.isUrl("https://example.com/path?q=1"));
    }

    @Test
    void isUrlRejectsNullBlankAndNonHttpSchemes() {
        assertFalse(ValidateUtils.isUrl(null));
        assertFalse(ValidateUtils.isUrl(""));
        assertFalse(ValidateUtils.isUrl("ftp://files.com"));
        assertFalse(ValidateUtils.isUrl("not-a-url"));
    }

    // ---------- page / size ----------

    @Test
    void isValidPageRejectsLessThanOne() {
        assertFalse(ValidateUtils.isValidPage(0));
        assertTrue(ValidateUtils.isValidPage(1));
        assertTrue(ValidateUtils.isValidPage(10));
    }

    @Test
    void isValidPageSizeBounds() {
        assertFalse(ValidateUtils.isValidPageSize(0));
        assertTrue(ValidateUtils.isValidPageSize(1));
        assertTrue(ValidateUtils.isValidPageSize(50));
        assertFalse(ValidateUtils.isValidPageSize(51));
    }

    // ---------- normalizePage / normalizePageSize ----------

    @Test
    void normalizePageReturnsDefaultForNullAndValidates() {
        assertEquals(ValidateUtils.DEFAULT_PAGE, ValidateUtils.normalizePage(null));
        assertEquals(3, ValidateUtils.normalizePage(3));
    }

    @Test
    void normalizePageRejectsInvalidInput() {
        assertThrows(BusinessException.class, () -> ValidateUtils.normalizePage(0));
    }

    @Test
    void normalizePageSizeReturnsDefaultForNullAndValidates() {
        assertEquals(ValidateUtils.DEFAULT_SIZE, ValidateUtils.normalizePageSize(null));
        assertEquals(20, ValidateUtils.normalizePageSize(20));
    }

    @Test
    void normalizePageSizeRejectsInvalidInput() {
        assertThrows(BusinessException.class, () -> ValidateUtils.normalizePageSize(0));
        assertThrows(BusinessException.class, () -> ValidateUtils.normalizePageSize(51));
    }

    // ---------- requireNotBlank / requirePositive ----------

    @Test
    void requireNotBlankThrowsOnBlank() {
        assertThrows(BusinessException.class, () -> ValidateUtils.requireNotBlank(null, "name"));
        assertThrows(BusinessException.class, () -> ValidateUtils.requireNotBlank(" ", "name"));
    }

    @Test
    void requireNotBlankDoesNotThrowOnNonBlank() {
        assertDoesNotThrow(() -> ValidateUtils.requireNotBlank("hello", "name"));
    }

    @Test
    void requirePositiveThrowsOnNonPositive() {
        assertThrows(BusinessException.class, () -> ValidateUtils.requirePositive(null, "id"));
        assertThrows(BusinessException.class, () -> ValidateUtils.requirePositive(0L, "id"));
    }

    @Test
    void requirePositiveDoesNotThrowOnPositive() {
        assertDoesNotThrow(() -> ValidateUtils.requirePositive(1L, "id"));
    }
}
