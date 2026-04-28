package com.campushub.review.event;

import com.campushub.order.event.OrderCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCompletedEventListener {

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        // Scaffold only. Event handling will be implemented later.
    }
}