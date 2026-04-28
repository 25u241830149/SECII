package com.campushub.user.service;

public interface CreditService {

    Integer getCreditScore(Long userId);

    void updateCreditScore(Long userId, Integer score);
}