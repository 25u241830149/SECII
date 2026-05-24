package com.campushub.order.event;

public record OrderConfirmedEvent(
        Long orderId,
        Long taskId,
        Long posterId,
        Long helperId
) {
}
