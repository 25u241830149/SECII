package com.campushub.order.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.service.GrabService;
import com.campushub.order.service.OrderService;
import com.campushub.order.service.OrderStatusService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final GrabService grabService;
    private final OrderStatusService orderStatusService;

    @PostMapping("/grab")
    public ApiResponse<String> grabOrder(@RequestBody GrabOrderRequest request) {
        grabService.grabOrder(request);
        return ApiResponse.success("Order module scaffold ready", null);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailDTO> getOrderDetail(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.getOrderDetail(orderId));
    }

    @PostMapping("/{orderId}/confirm")
    public ApiResponse<String> confirmOrder(@PathVariable Long orderId) {
        orderStatusService.confirmOrder(orderId);
        return ApiResponse.success("Order confirm scaffold ready", null);
    }

    @PostMapping("/{orderId}/complete")
    public ApiResponse<String> completeOrder(@PathVariable Long orderId) {
        orderStatusService.completeOrder(orderId);
        return ApiResponse.success("Order complete scaffold ready", null);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<String> cancelOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        orderStatusService.cancelOrder(orderId, userId);
        return ApiResponse.success("Order cancel scaffold ready", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderListDTO>> getOrderList(@RequestParam(defaultValue = "1") Long page,
                                                                @RequestParam(defaultValue = "10") Long size,
                                                                @RequestParam(required = false) Long userId,
                                                                @RequestParam(required = false) String role,
                                                                @RequestParam(required = false) String status) {
        List<OrderListDTO> orders = orderService.listOrders(userId, role, status);
        return ApiResponse.success(new PageResponse<>(orders, (long) orders.size(), page, size));
    }
}