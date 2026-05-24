package com.campushub.task.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import java.util.Locale;

public final class TaskCodecs {

    public static final int TASK_STATUS_OPEN = 0;
    public static final int TASK_STATUS_LOCKED = 1;
    public static final int TASK_STATUS_IN_PROGRESS = 2;
    public static final int TASK_STATUS_COMPLETED = 3;
    public static final int TASK_STATUS_OFFLINE = 4;

    private TaskCodecs() {
    }

    public static int categoryCode(String category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "任务分类不能为空");
        }
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "EXPRESS" -> 0;
            case "STUDY" -> 1;
            case "SECOND_HAND" -> 2;
            case "TEAM_UP" -> 3;
            case "OTHER" -> 4;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的任务分类");
        };
    }

    public static String sortKey(String sort) {
        if (sort == null || sort.isBlank()) {
            return "time";
        }
        String normalized = sort.trim().toLowerCase(Locale.ROOT);
        if (!"time".equals(normalized) && !"hot".equals(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的排序方式");
        }
        return normalized;
    }
}
