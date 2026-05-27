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
    public static final int TASK_STATUS_CANCELLED = TASK_STATUS_OFFLINE;

    public static final int TASK_CATEGORY_EXPRESS = 0;
    public static final int TASK_CATEGORY_STUDY = 1;
    public static final int TASK_CATEGORY_SECOND_HAND = 2;
    public static final int TASK_CATEGORY_TEAM_UP = 3;
    public static final int TASK_CATEGORY_OTHER = 4;

    private TaskCodecs() {
    }

    public static int categoryCode(String category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "任务分类不能为空");
        }
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "EXPRESS" -> TASK_CATEGORY_EXPRESS;
            case "STUDY" -> TASK_CATEGORY_STUDY;
            case "SECOND_HAND" -> TASK_CATEGORY_SECOND_HAND;
            case "TEAM_UP" -> TASK_CATEGORY_TEAM_UP;
            case "OTHER" -> TASK_CATEGORY_OTHER;
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

    public static String statusKey(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"OPEN".equals(normalized)
                && !"PENDING_CONFIRM".equals(normalized)
                && !"LOCKED".equals(normalized)
                && !"IN_PROGRESS".equals(normalized)
                && !"COMPLETED".equals(normalized)
                && !"OFFLINE".equals(normalized)
                && !"CANCELLED".equals(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的任务状态");
        }
        return "LOCKED".equals(normalized) ? "PENDING_CONFIRM" : normalized;
    }

    public static String rewardTypeKey(String rewardType) {
        if (rewardType == null || rewardType.isBlank() || "ALL".equalsIgnoreCase(rewardType.trim())) {
            return null;
        }
        String normalized = rewardType.trim().toUpperCase(Locale.ROOT);
        if (!"PAID".equals(normalized) && !"FREE".equals(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的报酬筛选");
        }
        return normalized;
    }

    public static String locationTypeKey(String locationType) {
        if (locationType == null || locationType.isBlank() || "ALL".equalsIgnoreCase(locationType.trim())) {
            return null;
        }
        String normalized = locationType.trim().toUpperCase(Locale.ROOT);
        if (!"WITH_LOCATION".equals(normalized) && !"UNSPECIFIED".equals(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的地点筛选");
        }
        return normalized;
    }
}
