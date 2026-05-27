package com.campushub.order.event;

public record OrderCancelledEvent(
        Long orderId,
        Long taskId,
        Long posterId,
        Long helperId,
        String taskTitle
) {
}
