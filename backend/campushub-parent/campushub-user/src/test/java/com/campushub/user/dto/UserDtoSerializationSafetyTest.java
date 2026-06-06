package com.campushub.user.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UserDtoSerializationSafetyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void publicUserJsonDoesNotExposeSensitiveAccountFields() throws Exception {
        UserPublicDTO dto = new UserPublicDTO(7L, "Alice", "avatar.png", null, 96, "trusted");

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("Alice"));
        assertFalse(json.contains("studentId"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("phone"));
        assertFalse(json.contains("email"));
    }

    @Test
    void homeJsonDoesNotExposeStudentIdOrContactFields() throws Exception {
        UserHomeDTO dto = new UserHomeDTO(7L, "Alice", "avatar.png", 96, "trusted", 0, 0, 0, null);

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("publishedTaskCount"));
        assertFalse(json.contains("studentId"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("phone"));
        assertFalse(json.contains("email"));
    }
}
