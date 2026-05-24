package com.campushub.task.dto;

import java.math.BigDecimal;

public record TaskCreateRequest(
        String title,
        String description,
        String category,
        String location,
        BigDecimal reward,
        Double longitude,
        Double latitude
) {
}
