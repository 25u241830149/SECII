package com.campushub.message.dto;

import java.time.OffsetDateTime;

public record ChatMessageDTO(
        Long messageId,
        Long orderId,
        Long senderId,
        String senderName,
        String senderAvatarUrl,
        Long receiverId,
        String receiverName,
        String receiverAvatarUrl,
        String content,
        Boolean read,
        OffsetDateTime createdAt
) {
}
