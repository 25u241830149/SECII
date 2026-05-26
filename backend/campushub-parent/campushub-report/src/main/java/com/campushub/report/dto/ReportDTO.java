package com.campushub.report.dto;

import java.time.OffsetDateTime;

public record ReportDTO(
        Long reportId,
        Long reporterId,
        String reporterName,
        Long targetUserId,
        String targetUserName,
        Long handlerId,
        String handlerName,
        Long taskId,
        Long orderId,
        Long postId,
        Long commentId,
        String targetType,
        Long targetId,
        String reason,
        String status,
        String result,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
