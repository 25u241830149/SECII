package com.campushub.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSendRequest {

    private Long senderId;

    private Long receiverId;

    private Long orderId;

    private String content;
}