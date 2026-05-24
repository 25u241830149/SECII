package com.campushub.order.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.entity.Order;
import com.campushub.order.mapper.OrderMapper;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public Order requireOrder(Long orderId) {
        ValidateUtils.requirePositive(orderId, "orderId");
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    public OrderDetailDTO getDetail(Long orderId, Long currentUserId) {
        Order order = requireAccessibleOrder(orderId, currentUserId);
        OrderDetailDTO detail = orderMapper.selectOrderDetail(order.getId());
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return detail;
    }

    public PageResponse<OrderListDTO> list(Long userId, String role, String status, Integer page, Integer size) {
        SecurityUtils.requireCurrentUser(userId);
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        String normalizedRole = OrderCodecs.normalizeRole(role);
        Integer statusCode = status == null || status.isBlank() ? null : OrderCodecs.statusCode(status);
        int offset = (normalizedPage - 1) * normalizedSize;
        var records = orderMapper.selectOrderList(userId, normalizedRole, statusCode, offset, normalizedSize);
        long total = orderMapper.countOrderList(userId, normalizedRole, statusCode);
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }

    public Order requireAccessibleOrder(Long orderId, Long currentUserId) {
        Order order = requireOrder(orderId);
        if (!SecurityUtils.isAdmin()
                && !Objects.equals(order.getPosterId(), currentUserId)
                && !Objects.equals(order.getHelperId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN_MESSAGE);
        }
        return order;
    }
}
