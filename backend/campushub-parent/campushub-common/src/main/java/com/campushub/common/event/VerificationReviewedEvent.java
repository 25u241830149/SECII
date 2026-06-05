package com.campushub.common.event;

public record VerificationReviewedEvent(
        Long userId,
        boolean approved,
        String remark
) {
}
