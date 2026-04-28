package com.campushub.message.service;

public interface MessageService {

    Integer getUnreadCount(Long userId);

    void markAsRead(Long messageId);

    void deleteMessage(Long messageId);
}