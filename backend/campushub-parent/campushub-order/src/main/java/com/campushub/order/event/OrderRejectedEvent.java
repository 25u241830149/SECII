package com.campushub.order.event;

public record OrderRejectedEvent(Long orderId, Long taskId, Long posterId, Long helperId, String taskTitle, boolean teamUp) {
}
