package com.campushub.user.dto;

public record UserInfoDTO(
        Long userId,
        String studentId,
        String email,
        String phone,
        String nickname,
        String realName,
        String department,
        String avatarUrl,
        String role,
        Integer creditScore,
        String verificationStatus
) {
}
