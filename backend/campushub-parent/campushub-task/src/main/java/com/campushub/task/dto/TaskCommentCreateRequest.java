package com.campushub.task.dto;

public record TaskCommentCreateRequest(
        String content,
        Long parentCommentId,
        Long replyToUserId
) {
}
