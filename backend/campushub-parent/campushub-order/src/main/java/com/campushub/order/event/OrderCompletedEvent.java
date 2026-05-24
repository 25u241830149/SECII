package com.campushub.order.event;

public record OrderCompletedEvent(
        Long orderId,
        Long taskId,
        Long posterId,
        Long helperId
) {
}
