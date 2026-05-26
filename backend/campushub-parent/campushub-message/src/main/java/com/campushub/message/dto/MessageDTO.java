package com.campushub.message.dto;

import java.time.OffsetDateTime;

public record MessageDTO(
        Long messageId,
        Long receiverId,
        String type,
        String title,
        String content,
        Boolean read,
        OffsetDateTime createdAt
) {
}
