package com.campushub.task.dto;

import java.time.OffsetDateTime;

public record TaskCommentDTO(
        Long commentId,
        Long taskId,
        Long authorId,
        String authorName,
        String authorAvatarUrl,
        String content,
        OffsetDateTime createdAt
) {
}
