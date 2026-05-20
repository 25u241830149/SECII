package com.campushub.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginRequest(
        @JsonAlias({"username", "studentNo"})
        String studentId,
        String password
) {
}
