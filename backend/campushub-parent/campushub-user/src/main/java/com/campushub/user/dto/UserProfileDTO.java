package com.campushub.user.dto;

import java.time.LocalDateTime;

public record UserProfileDTO(
        Long userId,
        String studentId,
        String nickname,
        String avatarUrl,
        String role,
        Integer creditScore,
        String verificationStatus,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
