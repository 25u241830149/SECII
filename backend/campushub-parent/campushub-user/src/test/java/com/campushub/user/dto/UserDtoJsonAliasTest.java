package com.campushub.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UserDtoJsonAliasTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginRequestAcceptsUsernameAndStudentNoAliases() throws Exception {
        LoginRequest usernameRequest = objectMapper.readValue(
                """
                {"username":"20260001","password":"CampusHub123"}
                """,
                LoginRequest.class
        );
        LoginRequest studentNoRequest = objectMapper.readValue(
                """
                {"studentNo":"20260002","password":"CampusHub123"}
                """,
                LoginRequest.class
        );

        assertEquals("20260001", usernameRequest.studentId());
        assertEquals("20260002", studentNoRequest.studentId());
    }

    @Test
    void registerRequestAcceptsStudentCardAliases() throws Exception {
        RegisterRequest request = objectMapper.readValue(
                """
                {
                  "studentNo":"20260001",
                  "password":"CampusHub123",
                  "nickname":"Alice",
                  "realName":"张三",
                                                                        "department":"软件学院",
                  "documentUrl":"student-cards/20260001.jpg"
                }
                """,
                RegisterRequest.class
        );

        assertEquals("20260001", request.studentId());
        assertEquals("软件学院", request.department());
        assertEquals("student-cards/20260001.jpg", request.studentCardImage());
    }

    @Test
    void verificationSubmitRequestAcceptsDocumentUrlAlias() throws Exception {
        VerificationSubmitRequest request = objectMapper.readValue(
                """
                {
                  "userId":7,
                  "realName":"张三",
                  "studentId":"20260001",
                  "college":"软件学院",
                  "documentUrl":"student-cards/7.jpg"
                }
                """,
                VerificationSubmitRequest.class
        );

        assertEquals("student-cards/7.jpg", request.studentCardImage());
    }

    @Test
    void profileUpdateRequestAcceptsAvatarAlias() throws Exception {
        UserProfileUpdateRequest request = objectMapper.readValue(
                """
                {
                  "email":"alice@example.com",
                  "phone":"13800000000",
                  "nickname":"Alice",
                  "avatar":"/uploads/avatars/7/avatar.png"
                }
                """,
                UserProfileUpdateRequest.class
        );
                assertEquals("alice@example.com", request.email());
                assertEquals("13800000000", request.phone());
                assertEquals("/uploads/avatars/7/avatar.png", request.avatarUrl());
    }
}
