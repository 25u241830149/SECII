package com.campushub.task.dto;

import java.time.OffsetDateTime;

public record TaskCommentDTO(
        Long commentId,
        Long taskId,
        Long authorId,
        String authorName,
        String authorAvatarUrl,
        Long parentCommentId,
        Long replyToUserId,
        String replyToUserName,
        String content,
        Integer likeCount,
        Boolean likedByMe,
        OffsetDateTime createdAt
) {
}
