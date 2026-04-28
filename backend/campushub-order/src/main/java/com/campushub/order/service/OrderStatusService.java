package com.campushub.order.service;

public interface OrderStatusService {

    void confirmOrder(Long orderId);

    void completeOrder(Long orderId);

    void cancelOrder(Long orderId, Long userId);
}