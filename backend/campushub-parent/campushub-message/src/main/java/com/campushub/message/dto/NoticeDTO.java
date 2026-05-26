package com.campushub.message.dto;

import java.time.OffsetDateTime;

public record NoticeDTO(
        Long noticeId,
        Long publisherId,
        String publisherName,
        String title,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
