package com.campushub.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EncryptUtilsTest {

    @Test
    void encryptPasswordGeneratesBcryptWithSalt() {
        String encrypted = EncryptUtils.encryptPassword("CampusHub123");

        assertTrue(encrypted.startsWith("$2a$") || encrypted.startsWith("$2b$"));
        // Same password must produce different hashes due to salt
        assertNotEquals(encrypted, EncryptUtils.encryptPassword("CampusHub123"));
    }

    @Test
    void encryptPasswordRejectsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> EncryptUtils.encryptPassword(null));
        assertThrows(IllegalArgumentException.class, () -> EncryptUtils.encryptPassword(""));
        assertThrows(IllegalArgumentException.class, () -> EncryptUtils.encryptPassword("   "));
    }

    @Test
    void matchesPasswordReturnsTrueForCorrectPassword() {
        String encrypted = EncryptUtils.encryptPassword("CampusHub123");

        assertTrue(EncryptUtils.matchesPassword("CampusHub123", encrypted));
    }

    @Test
    void matchesPasswordReturnsFalseForWrongPassword() {
        String encrypted = EncryptUtils.encryptPassword("CampusHub123");

        assertFalse(EncryptUtils.matchesPassword("wrong", encrypted));
    }

    @Test
    void matchesPasswordReturnsFalseForNullInputs() {
        String encrypted = EncryptUtils.encryptPassword("CampusHub123");

        assertFalse(EncryptUtils.matchesPassword(null, encrypted));
        assertFalse(EncryptUtils.matchesPassword("pass", null));
        assertFalse(EncryptUtils.matchesPassword("pass", ""));
    }

    @Test
    void passwordNeedsRehashDetectsUpgrade() {
        assertTrue(EncryptUtils.passwordNeedsRehash(null));
        assertFalse(EncryptUtils.passwordNeedsRehash(EncryptUtils.encryptPassword("pass")));
    }

    @Test
    void maskEmailRedactsLocalPart() {
        assertEquals("a***e@example.com", EncryptUtils.maskEmail("alice@example.com"));
        assertEquals("a*@x.cn", EncryptUtils.maskEmail("ab@x.cn"));       // local <= 2
        assertEquals("t***t@mail.campus.cn", EncryptUtils.maskEmail("test@mail.campus.cn"));
    }

    @Test
    void maskEmailReturnsOriginalWhenNotEmail() {
        assertEquals("not-email", EncryptUtils.maskEmail("not-email"));
    }

    @Test
    void maskPhoneRedactsMiddleDigits() {
        assertEquals("138****0000", EncryptUtils.maskPhone("13800000000"));
    }

    @Test
    void maskPhoneReturnsOriginalWhenNotPhone() {
        assertEquals("abc", EncryptUtils.maskPhone("abc"));
    }
}
