package com.campushub.order.event;

public record OrderAbandonedEvent(Long orderId, Long taskId, Long posterId, Long helperId, String taskTitle, boolean teamUp) {
}
