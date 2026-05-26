package com.campushub.review.dto;

public record ReviewCreateRequest(
        Long orderId,
        Long targetUserId,
        Integer rating,
        String content
) {
}
