package com.campushub.order.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import java.util.Locale;

final class OrderCodecs {

    static final int ORDER_STATUS_PENDING = 0;
    static final int ORDER_STATUS_CONFIRMED = 1;
    static final int ORDER_STATUS_COMPLETED = 2;
    static final int ORDER_STATUS_CANCELLED = 3;

    private OrderCodecs() {
    }

    static int statusCode(String status) {
        if (status == null || status.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不能为空");
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> ORDER_STATUS_PENDING;
            case "CONFIRMED" -> ORDER_STATUS_CONFIRMED;
            case "COMPLETED" -> ORDER_STATUS_COMPLETED;
            case "CANCELLED" -> ORDER_STATUS_CANCELLED;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的订单状态");
        };
    }

    static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if (!"poster".equals(normalized) && !"helper".equals(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色过滤参数不合法");
        }
        return normalized;
    }
}
