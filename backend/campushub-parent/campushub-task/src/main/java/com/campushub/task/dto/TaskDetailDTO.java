package com.campushub.task.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TaskDetailDTO(
        Long taskId,
        String title,
        String description,
        String category,
        String status,
        BigDecimal reward,
        String location,
        Double longitude,
        Double latitude,
        OffsetDateTime deadlineTime,
        String itemImageUrl,
        BigDecimal originalPrice,
        Integer teamTotalMembers,
        Integer teamCurrentMembers,
        OffsetDateTime activityTime,
        String activityNote,
        Long publisherId,
        String publisherName,
        String publisherAvatarUrl,
        String publisherDepartment,
        Integer publisherCreditScore,
        Integer favoriteCount,
        Boolean favorited,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
