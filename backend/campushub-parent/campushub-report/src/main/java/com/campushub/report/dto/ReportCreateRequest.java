package com.campushub.report.dto;

public record ReportCreateRequest(
        String targetType,
        Long targetId,
        Long targetUserId,
        String reason
) {
}
