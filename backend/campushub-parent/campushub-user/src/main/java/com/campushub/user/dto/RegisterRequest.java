package com.campushub.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record RegisterRequest(
        @JsonAlias({"username", "studentNo"})
        String studentId,
        String password,
        String nickname,
        String realName,
        String department,
        @JsonAlias({"documentUrl", "studentCardUrl"})
        String studentCardImage
) {
}
