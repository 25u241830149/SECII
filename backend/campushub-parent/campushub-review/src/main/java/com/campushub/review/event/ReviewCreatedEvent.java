package com.campushub.review.event;

public record ReviewCreatedEvent(
        Long reviewId,
        Long orderId,
        Long reviewerId,
        Long targetUserId,
        Integer rating
) {
}
