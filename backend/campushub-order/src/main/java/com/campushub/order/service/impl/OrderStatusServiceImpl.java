package com.campushub.order.service.impl;

import com.campushub.order.service.OrderStatusService;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusServiceImpl implements OrderStatusService {

    @Override
    public void confirmOrder(Long orderId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void completeOrder(Long orderId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void cancelOrder(Long orderId, Long userId) {
        // Scaffold only. Business logic will be implemented later.
    }
}