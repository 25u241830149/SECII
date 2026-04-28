package com.campushub.order.service;

import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import java.util.List;

public interface OrderService {

    OrderDetailDTO getOrderDetail(Long orderId);

    List<OrderListDTO> listOrders(Long userId, String role, String status);
}