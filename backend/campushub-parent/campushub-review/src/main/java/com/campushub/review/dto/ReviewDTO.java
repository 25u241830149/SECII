package com.campushub.review.dto;

import java.time.OffsetDateTime;

public record ReviewDTO(
        Long reviewId,
        Long orderId,
        Long reviewerId,
        String reviewerName,
        String reviewerAvatarUrl,
        Long targetUserId,
        String targetUserName,
        String targetUserAvatarUrl,
        Integer rating,
        String content,
        OffsetDateTime createdAt
) {
}
