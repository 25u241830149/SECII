package com.campushub.user.dto;

public record AdminUserOptionDTO(
        Long userId,
        String studentId,
        String nickname,
        String role,
        String status,
        Integer creditScore
) {
}
