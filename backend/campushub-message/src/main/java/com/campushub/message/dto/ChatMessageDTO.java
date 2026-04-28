package com.campushub.message.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private Long id;

    private Long senderId;

    private Long receiverId;

    private Long orderId;

    private String content;

    private Boolean read;

    private LocalDateTime createdAt;
}