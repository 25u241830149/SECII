package com.campushub.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class UserDtoAssemblerTest {

    @Test
    void toUserInfoMapsAllFields() {
        UserInfoDTO dto = UserDtoAssembler.toUserInfo(user(96), verification(1));

        assertEquals(7L, dto.userId());
        assertEquals("20260001", dto.studentId());
        assertEquals("alice@example.com", dto.email());
        assertEquals("13800000000", dto.phone());
        assertEquals("Alice", dto.nickname());
        assertEquals("Alice Real", dto.realName());
        assertEquals("Software", dto.department());
        assertEquals("/uploads/avatars/7/a.png", dto.avatarUrl());
        assertEquals("ADMIN", dto.role());
        assertEquals(96, dto.creditScore());
        assertEquals("APPROVED", dto.verificationStatus());
    }

    @Test
    void toUserProfileMapsAuditFields() {
        UserProfileDTO dto = UserDtoAssembler.toUserProfile(user(80), verification(2));

        assertEquals(OffsetDateTime.parse("2026-06-05T10:00:00+08:00"), dto.createTime());
        assertEquals(OffsetDateTime.parse("2026-06-05T12:00:00+08:00"), dto.updateTime());
        assertEquals("REJECTED", dto.verificationStatus());
        assertEquals("ADMIN", dto.role());
    }

    @Test
    void toUserPublicDerivesCreditLevelAtBoundaries() {
        UserPublicDTO reliable = UserDtoAssembler.toUserPublic(user(80));
        UserPublicDTO ordinary = UserDtoAssembler.toUserPublic(user(60));
        UserPublicDTO warning = UserDtoAssembler.toUserPublic(user(59));

        assertEquals(CreditService.creditLevel(80), reliable.creditLevel());
        assertEquals(CreditService.creditLevel(60), ordinary.creditLevel());
        assertEquals(CreditService.creditLevel(59), warning.creditLevel());
    }

    @Test
    void nullVerificationFallsBackToPendingAndNullScoreStaysNull() {
        User user = user(null);

        UserInfoDTO info = UserDtoAssembler.toUserInfo(user, null);
        UserPublicDTO publicDto = UserDtoAssembler.toUserPublic(user);

        assertEquals("PENDING", info.verificationStatus());
        assertNull(publicDto.creditScore());
        assertEquals(CreditService.creditLevel(0), publicDto.creditLevel());
    }

    private static User user(Integer creditScore) {
        User user = new User();
        user.setId(7L);
        user.setStudentId("20260001");
        user.setEmail("alice@example.com");
        user.setPhone("13800000000");
        user.setNickname("Alice");
        user.setRealName("Alice Real");
        user.setDepartment("Software");
        user.setAvatarUrl("/uploads/avatars/7/a.png");
        user.setRole(1);
        user.setCreditScore(creditScore);
        user.setCreateTime(OffsetDateTime.parse("2026-06-05T10:00:00+08:00"));
        user.setUpdateTime(OffsetDateTime.parse("2026-06-05T12:00:00+08:00"));
        return user;
    }

    private static UserVerification verification(Integer status) {
        UserVerification verification = new UserVerification();
        verification.setStatus(status);
        return verification;
    }
}
