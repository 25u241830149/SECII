package com.campushub.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCompletedEvent {

    private Long orderId;
}