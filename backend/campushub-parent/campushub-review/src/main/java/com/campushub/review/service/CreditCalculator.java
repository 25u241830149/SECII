package com.campushub.review.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class CreditCalculator {

    public int calculateScore(BigDecimal averageRating, long reviewCount) {
        if (reviewCount <= 0 || averageRating == null) {
            return 100;
        }
        int score = averageRating.multiply(BigDecimal.valueOf(20))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        return Math.max(40, Math.min(100, score));
    }

    public String level(int creditScore) {
        if (creditScore >= 90) {
            return "诚信学生";
        }
        if (creditScore >= 80) {
            return "可靠同学";
        }
        if (creditScore >= 60) {
            return "普通用户";
        }
        return "信用预警";
    }
}
