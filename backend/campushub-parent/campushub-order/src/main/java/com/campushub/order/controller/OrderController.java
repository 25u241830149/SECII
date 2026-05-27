package com.campushub.order.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.dto.OrderStatsDTO;
import com.campushub.order.service.GrabService;
import com.campushub.order.service.OrderService;
import com.campushub.order.service.OrderStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final GrabService grabService;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;

    public OrderController(
            GrabService grabService,
            OrderService orderService,
            OrderStatusService orderStatusService
    ) {
        this.grabService = grabService;
        this.orderService = orderService;
        this.orderStatusService = orderStatusService;
    }

    @PostMapping("/grab")
    public ApiResponse<OrderDetailDTO> grab(@RequestBody GrabOrderRequest request) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(grabService.grab(request, currentUserId));
    }

    @PostMapping("/{orderId}/confirm")
    public ApiResponse<OrderDetailDTO> confirm(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(orderStatusService.confirm(orderId, currentUserId));
    }

    @PostMapping("/{orderId}/complete")
    public ApiResponse<OrderDetailDTO> complete(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(orderStatusService.complete(orderId, currentUserId));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderDetailDTO> cancel(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(orderStatusService.cancel(orderId, currentUserId));
    }

    @GetMapping("/summary/stats")
    public ApiResponse<OrderStatsDTO> summaryStats(@RequestParam Long userId) {
        return ApiResponse.success(orderService.stats(userId));
    }

    @GetMapping("/stats")
    public ApiResponse<OrderStatsDTO> stats(@RequestParam Long userId) {
        return ApiResponse.success(orderService.stats(userId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailDTO> detail(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(orderService.getDetail(orderId, currentUserId));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderListDTO>> list(
            @RequestParam Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(orderService.list(userId, role, status, page, size));
    }
}
