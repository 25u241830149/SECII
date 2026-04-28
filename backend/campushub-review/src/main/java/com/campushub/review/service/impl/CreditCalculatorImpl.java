package com.campushub.review.service.impl;

import com.campushub.review.service.CreditCalculator;
import org.springframework.stereotype.Service;

@Service
public class CreditCalculatorImpl implements CreditCalculator {

    @Override
    public Integer calculate(Integer currentScore, Integer reviewScore) {
        int base = currentScore == null ? 100 : currentScore;
        int delta = reviewScore == null ? 0 : reviewScore - 3;
        return base + delta;
    }
}