package com.campushub.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderDetailDTO(
        Long orderId,
        Long taskId,
        String taskTitle,
        String taskDescription,
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
        Integer posterCreditScore,
        Long helperId,
        String helperName,
        String helperAvatarUrl,
        Integer helperCreditScore,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
