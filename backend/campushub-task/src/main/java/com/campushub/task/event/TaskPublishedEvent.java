package com.campushub.task.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskPublishedEvent {

    private Long taskId;

    private Long publisherId;
}