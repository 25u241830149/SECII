package com.campushub.task.event;

public record TaskCancelledEvent(
        Long taskId,
        Long publisherId,
        String taskTitle
) {
}
