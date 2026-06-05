package com.campushub.common.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campushub.common.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter();
        filter.setEnabled(true);
    }

    @Test
    void validateDoesNotThrowWhenNoWordsMatched() {
        filter.validate("This is clean content");
    }

    @Test
    void validateThrowsWhenSensitiveWordMatched() {
        filter.addWord("spam");

        assertThrows(BusinessException.class, () -> filter.validate("this contains spam word"));
    }

    @Test
    void validateDoesNotThrowWhenDisabled() {
        filter.addWord("spam");
        filter.setEnabled(false);

        filter.validate("this contains spam but filter is off");
    }

    @Test
    void containsSensitiveWordReturnsTrueForMatch() {
        filter.addWords(List.of("spam", "scam"));

        assertTrue(filter.containsSensitiveWord("this is a scam alert"));
        assertFalse(filter.containsSensitiveWord("clean content"));
    }

    @Test
    void containsSensitiveWordReturnsFalseWhenDisabled() {
        filter.addWord("spam");
        filter.setEnabled(false);

        assertFalse(filter.containsSensitiveWord("spam here"));
    }

    @Test
    void containsSensitiveWordReturnsFalseForNullOrBlank() {
        filter.addWord("spam");

        assertFalse(filter.containsSensitiveWord(null));
        assertFalse(filter.containsSensitiveWord(""));
        assertFalse(filter.containsSensitiveWord("   "));
    }

    @Test
    void findSensitiveWordsReturnsMatchedWords() {
        filter.addWords(List.of("spam", "scam", "ads"));

        List<String> matched = filter.findSensitiveWords("this is spam and also scam!");

        assertEquals(2, matched.size());
        assertTrue(matched.contains("spam"));
        assertTrue(matched.contains("scam"));
    }

    @Test
    void findSensitiveWordsReturnsEmptyWhenNoWordsConfigured() {
        List<String> matched = filter.findSensitiveWords("anything");

        assertNotNull(matched);
        assertTrue(matched.isEmpty());
    }

    @Test
    void filterReplacesSensitiveWordsWithReplacement() {
        filter.addWord("spam");
        filter.setReplacement("###");

        String filtered = filter.filter("this spam is filtered");

        assertFalse(filtered.contains("spam"));
        assertTrue(filtered.contains("###"));
    }

    @Test
    void filterReturnsOriginalWhenDisabled() {
        filter.addWord("spam");
        filter.setEnabled(false);

        assertEquals("spam here", filter.filter("spam here"));
    }

    @Test
    void filterReturnsOriginalForNullOrBlank() {
        filter.addWord("spam");

        assertEquals("", filter.filter(""));
        assertEquals("   ", filter.filter("   "));
    }

    @Test
    void addWordIgnoresBlank() {
        filter.addWord("   ");

        assertTrue(filter.findSensitiveWords("anything").isEmpty());
    }

    @Test
    void addWordsIgnoresNullCollection() {
        filter.addWords(null);

        assertTrue(filter.findSensitiveWords("anything").isEmpty());
    }

    @Test
    void normalizationIsCaseInsensitive() {
        filter.addWord("SPAM");

        assertTrue(filter.containsSensitiveWord("spam here"));
        assertTrue(filter.containsSensitiveWord("SPAM here"));
    }

    @Test
    void validateRejectsContentWithSensitiveWordAfterAdd() {
        filter.addWord("gambling");

        assertThrows(BusinessException.class, () -> filter.validate("try gambling today"));
    }
}
