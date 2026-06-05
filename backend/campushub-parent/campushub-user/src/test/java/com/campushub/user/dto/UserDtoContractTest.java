package com.campushub.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserDtoContractTest {

    @Test
    void userPublicDtoContainsOnlyPublicProfileFields() {
        UserPublicDTO dto = new UserPublicDTO(
                7L,
                "Alice",
                "avatar.png",
                null,
                95,
                "诚信学生"
        );

        assertEquals(7L, dto.userId());
        assertEquals("Alice", dto.nickname());
        assertEquals("avatar.png", dto.avatarUrl());
        assertEquals(95, dto.creditScore());
        assertEquals("诚信学生", dto.creditLevel());
    }

    @Test
    void userHomeDtoContainsProfileAndAggregateCounters() {
        UserHomeDTO dto = new UserHomeDTO(
                7L,
                "Alice",
                "avatar.png",
                95,
                "诚信学生",
                0,
                0,
                0,
                null
        );

        assertEquals(7L, dto.userId());
        assertEquals("Alice", dto.nickname());
        assertEquals("avatar.png", dto.avatarUrl());
        assertEquals(95, dto.creditScore());
        assertEquals("诚信学生", dto.creditLevel());
        assertEquals(0, dto.publishedTaskCount());
        assertEquals(0, dto.completedOrderCount());
        assertEquals(0, dto.reviewCount());
    }
}
