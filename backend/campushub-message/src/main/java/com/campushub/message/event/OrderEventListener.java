package com.campushub.message.event;

import com.campushub.order.event.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        // Scaffold only. Event handling will be implemented later.
    }
}