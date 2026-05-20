package com.campushub.user.dto;

public record CreditDTO(
        Integer creditScore,
        String creditLevel,
        Double completedRate,
        Double cancelledRate
) {
}
