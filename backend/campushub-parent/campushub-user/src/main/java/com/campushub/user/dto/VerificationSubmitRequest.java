package com.campushub.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record VerificationSubmitRequest(
        Long userId,
        String realName,
        @JsonAlias("studentNo")
        String studentId,
        String college,
        @JsonAlias({"documentUrl", "studentCardUrl"})
        String studentCardImage
) {
}
