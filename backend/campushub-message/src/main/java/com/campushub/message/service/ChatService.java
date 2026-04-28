package com.campushub.message.service;

import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import java.util.List;

public interface ChatService {

    void sendMessage(ChatSendRequest request);

    List<ChatMessageDTO> getChatMessages(Long orderId, Long userId);
}