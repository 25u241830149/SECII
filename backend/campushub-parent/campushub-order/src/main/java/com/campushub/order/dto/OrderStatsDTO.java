package com.campushub.order.dto;

public record OrderStatsDTO(
        long waitingAcceptance,
        long pending,
        long inProgress,
        long waitingReview,
        long completed
) {
}
