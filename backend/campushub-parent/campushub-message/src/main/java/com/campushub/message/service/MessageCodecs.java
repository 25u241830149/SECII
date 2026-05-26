package com.campushub.message.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import java.util.Locale;

final class MessageCodecs {

    static final int TYPE_SYSTEM = 0;
    static final int TYPE_TASK = 1;
    static final int TYPE_ORDER = 2;
    static final int TYPE_REVIEW = 3;
    static final int TYPE_REPORT = 4;

    private MessageCodecs() {
    }

    static int typeCode(String type) {
        if (type == null || type.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息类型不能为空");
        }
        return switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "SYSTEM" -> TYPE_SYSTEM;
            case "TASK" -> TYPE_TASK;
            case "ORDER" -> TYPE_ORDER;
            case "REVIEW" -> TYPE_REVIEW;
            case "REPORT" -> TYPE_REPORT;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的消息类型");
        };
    }
}
