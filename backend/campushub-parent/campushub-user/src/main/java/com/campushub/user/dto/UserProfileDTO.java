package com.campushub.user.dto;

import java.time.OffsetDateTime;

public record UserProfileDTO(
        Long userId,
        String studentId,
        String nickname,
        String avatarUrl,
        String role,
        Integer creditScore,
        String verificationStatus,
        OffsetDateTime createTime,
        OffsetDateTime updateTime
) {
}
