package com.campushub.user.dto;

import java.time.OffsetDateTime;

public record UserProfileDTO(
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
        String verificationStatus,
        OffsetDateTime createTime,
        OffsetDateTime updateTime
) {
}
