package com.campushub.user.service.impl;

import com.campushub.user.service.CreditService;
import com.campushub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final UserService userService;

    @Override
    public Integer getCreditScore(Long userId) {
        return userService.getUserCreditScore(userId);
    }

    @Override
    public void updateCreditScore(Long userId, Integer score) {
        userService.updateUserCreditScore(userId, score);
    }
}