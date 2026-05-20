package com.campushub.user.dto;

public record UserPublicDTO(
        Long userId,
        String nickname,
        String avatarUrl,
        String department,
        Integer creditScore,
        String creditLevel
) {
}
