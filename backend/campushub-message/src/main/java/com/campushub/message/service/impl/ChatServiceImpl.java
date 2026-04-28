package com.campushub.message.service.impl;

import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import com.campushub.message.service.ChatService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Override
    public void sendMessage(ChatSendRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public List<ChatMessageDTO> getChatMessages(Long orderId, Long userId) {
        return Collections.emptyList();
    }
}