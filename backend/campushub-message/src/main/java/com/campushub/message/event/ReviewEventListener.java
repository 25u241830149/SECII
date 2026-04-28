package com.campushub.message.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventListener {

    @EventListener
    public void onReviewSubmitted(Object event) {
        // Scaffold only. Event handling will be implemented later.
    }
}