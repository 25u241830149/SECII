package com.campushub.task.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TaskUpdateRequest(
        String title,
        String description,
        String category,
        String location,
        BigDecimal reward,
        Double longitude,
        Double latitude,
        OffsetDateTime deadlineTime,
        String itemImageUrl,
        BigDecimal originalPrice,
        Integer teamTotalMembers,
        Integer teamCurrentMembers,
        OffsetDateTime activityTime,
        String activityNote
) {
}
