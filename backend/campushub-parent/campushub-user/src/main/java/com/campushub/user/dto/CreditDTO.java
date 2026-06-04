package com.campushub.user.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreditDTO(
        Integer creditScore,
        String creditLevel,
        Double completedRate,
        Double cancelledRate,
        BigDecimal averageRating,
        Long reviewCount,
        Long publishedTaskCount,
        Long completedOrderCount,
        Long cancelledOrderCount,
        List<CreditRecordDTO> recentRecords
) {
}
