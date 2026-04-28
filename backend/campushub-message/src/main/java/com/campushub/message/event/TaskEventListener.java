package com.campushub.message.event;

import com.campushub.task.event.TaskPublishedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener {

    @EventListener
    public void onTaskPublished(TaskPublishedEvent event) {
        // Scaffold only. Event handling will be implemented later.
    }
}