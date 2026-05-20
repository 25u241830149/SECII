package com.campushub.user.dto;

public record UserInfoDTO(
        Long userId,
        String studentId,
        String nickname,
        String avatarUrl,
        String role,
        Integer creditScore,
        String verificationStatus
) {
}
