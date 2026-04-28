package com.campushub.order.service.impl;

import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.service.OrderService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId) {
        return OrderDetailDTO.builder()
                .id(orderId)
                .status("PENDING")
                .build();
    }

    @Override
    public List<OrderListDTO> listOrders(Long userId, String role, String status) {
        return Collections.emptyList();
    }
}