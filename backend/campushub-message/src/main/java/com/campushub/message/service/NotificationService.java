package com.campushub.message.service;

public interface NotificationService {

    void notifyUser(Long userId, String content);
}