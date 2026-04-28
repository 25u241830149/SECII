package com.campushub.message.service.impl;

import com.campushub.message.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public Integer getUnreadCount(Long userId) {
        return 0;
    }

    @Override
    public void markAsRead(Long messageId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void deleteMessage(Long messageId) {
        // Scaffold only. Business logic will be implemented later.
    }
}