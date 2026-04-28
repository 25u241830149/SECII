package com.campushub.user.service;

import com.campushub.user.dto.VerificationSubmitRequest;

public interface VerificationService {

    void submitVerification(VerificationSubmitRequest request);

    void reviewVerification(Long userId, Long reviewerId, Boolean approved, String remark);
}