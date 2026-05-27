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

    static String statusKey(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"PENDING".equals(normalized)
                && !"CONFIRMED".equals(normalized)
                && !"COMPLETED".equals(normalized)
                && !"CANCELLED".equals(normalized)
                && !"WAITING_REVIEW".equals(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的订单状态");
        }
        return normalized;
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
