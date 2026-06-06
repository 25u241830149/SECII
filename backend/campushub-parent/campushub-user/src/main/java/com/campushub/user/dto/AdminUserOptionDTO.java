package com.campushub.user.dto;

public record AdminUserOptionDTO(
        Long userId,
        String studentId,
        String nickname,
        String role,
        String status,
        Integer creditScore,
        String verificationRealName,
        String verificationStatus,
        String verificationStudentCardImage,
        String verificationRemark,
        String verificationSubmittedAt
) {
}
