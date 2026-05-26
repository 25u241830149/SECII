package com.campushub.report.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import java.util.Locale;

final class ReportCodecs {

    static final int TARGET_USER = 0;
    static final int TARGET_TASK = 1;
    static final int TARGET_ORDER = 2;
    static final int TARGET_POST = 3;
    static final int TARGET_COMMENT = 4;

    static final int STATUS_PENDING = 0;
    static final int STATUS_HANDLED = 1;
    static final int STATUS_REJECTED = 2;

    private ReportCodecs() {
    }

    static int targetTypeCode(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "举报对象类型不能为空");
        }
        return switch (targetType.trim().toUpperCase(Locale.ROOT)) {
            case "USER" -> TARGET_USER;
            case "TASK" -> TARGET_TASK;
            case "ORDER" -> TARGET_ORDER;
            case "POST" -> TARGET_POST;
            case "COMMENT" -> TARGET_COMMENT;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的举报对象类型");
        };
    }

    static Integer statusCode(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> STATUS_PENDING;
            case "HANDLED" -> STATUS_HANDLED;
            case "REJECTED" -> STATUS_REJECTED;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的举报状态");
        };
    }

    static int handleStatusCode(String status) {
        Integer code = statusCode(status);
        if (code == null || code == STATUS_PENDING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "处理结果必须是 HANDLED 或 REJECTED");
        }
        return code;
    }
}
