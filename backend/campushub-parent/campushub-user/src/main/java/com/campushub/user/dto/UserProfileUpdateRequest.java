package com.campushub.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record UserProfileUpdateRequest(
        String email,
        String phone,
        String nickname,
        @JsonAlias("avatar")
        String avatarUrl
) {
}
