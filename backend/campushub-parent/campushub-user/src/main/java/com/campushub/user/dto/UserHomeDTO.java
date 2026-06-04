package com.campushub.user.dto;

import java.math.BigDecimal;

public record UserHomeDTO(
        Long userId,
        String nickname,
        String avatarUrl,
        Integer creditScore,
        String creditLevel,
        Integer publishedTaskCount,
        Integer completedOrderCount,
        Integer reviewCount,
        BigDecimal averageRating
) {
}
