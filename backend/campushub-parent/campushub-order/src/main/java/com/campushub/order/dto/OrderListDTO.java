package com.campushub.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderListDTO(
        Long orderId,
        Long taskId,
        String taskTitle,
        String taskCategory,
        String taskStatus,
        String taskLocation,
        BigDecimal reward,
        String status,
        Boolean currentUserReviewed,
        Integer teamTotalMembers,
        Integer teamCurrentMembers,
        Long posterId,
        String posterName,
        String posterAvatarUrl,
        Long helperId,
        String helperName,
        String helperAvatarUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
