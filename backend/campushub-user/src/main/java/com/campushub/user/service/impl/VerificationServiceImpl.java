package com.campushub.user.service.impl;

import com.campushub.user.dto.VerificationSubmitRequest;
import com.campushub.user.service.VerificationService;
import org.springframework.stereotype.Service;

@Service
public class VerificationServiceImpl implements VerificationService {

    @Override
    public void submitVerification(VerificationSubmitRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void reviewVerification(Long userId, Long reviewerId, Boolean approved, String remark) {
        // Scaffold only. Business logic will be implemented later.
    }
}