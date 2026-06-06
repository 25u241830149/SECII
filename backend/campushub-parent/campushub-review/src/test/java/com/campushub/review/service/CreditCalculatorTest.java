package com.campushub.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreditCalculatorTest {

    private final CreditCalculator creditCalculator = new CreditCalculator();

    @Test
    void calculateScoreFallsBackToDefaultWithoutReviews() {
        assertEquals(90, creditCalculator.calculateScore(null, 0));
    }

    @Test
    void calculateScoreRoundsAndClampsToBounds() {
        assertEquals(87, creditCalculator.calculateScore(BigDecimal.valueOf(4.35), 12));
        assertEquals(40, creditCalculator.calculateScore(BigDecimal.ONE, 2));
        assertEquals(100, creditCalculator.calculateScore(BigDecimal.valueOf(5), 8));
    }

    @Test
    void adjustmentAndLevelFollowCurrentRules() {
        assertEquals(3, creditCalculator.adjustmentForRating(5));
        assertEquals(-4, creditCalculator.adjustmentForRating(1));
        assertEquals("诚信学生", creditCalculator.level(95));
        assertEquals("可靠同学", creditCalculator.level(85));
        assertEquals("普通用户", creditCalculator.level(65));
        assertEquals("信用预警", creditCalculator.level(40));
    }
}
