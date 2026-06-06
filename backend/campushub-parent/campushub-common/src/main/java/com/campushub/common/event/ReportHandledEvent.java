package com.campushub.common.event;

public record ReportHandledEvent(
        Long reportId,
        Long reporterId,
        Long targetUserId,
        String targetType,
        Long targetId,
        String status,
        String result
) {
}
