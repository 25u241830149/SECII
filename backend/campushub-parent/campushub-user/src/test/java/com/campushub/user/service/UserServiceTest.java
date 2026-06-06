package com.campushub.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.AdminBanRequest;
import com.campushub.user.dto.AdminBanResultDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserProfileUpdateRequest;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import com.campushub.user.mapper.CreditMapper;
import com.campushub.user.mapper.UserMapper;
import com.campushub.user.mapper.UserVerificationMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserVerificationMapper verificationMapper;

    @Mock
    private CreditMapper creditMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getProfileReturnsUserAndLatestVerificationStatus() {
        User user = activeUser();
        UserVerification verification = new UserVerification();
        verification.setStatus(2);
        when(userMapper.selectActiveById(7L)).thenReturn(user);
        when(verificationMapper.selectLatestByUserId(7L)).thenReturn(verification);

        UserProfileDTO profile = userService.getProfile(7L);

        assertEquals(7L, profile.userId());
        assertEquals("20260001", profile.studentId());
        assertEquals("alice@example.com", profile.email());
        assertEquals("13800000000", profile.phone());
        assertEquals("Alice", profile.nickname());
        assertEquals("Alice Real", profile.realName());
        assertEquals("Software", profile.department());
        assertEquals("avatar.png", profile.avatarUrl());
        assertEquals(96, profile.creditScore());
        assertEquals("REJECTED", profile.verificationStatus());
    }

    @Test
    void getProfileRejectsMissingUser() {
        when(userMapper.selectActiveById(7L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getProfile(7L));

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void updateProfilePersistsEditableFieldsAndReturnsFreshProfile() {
        User existing = activeUser();
        User updated = activeUser();
        updated.setNickname("New Alice");
        updated.setEmail("new-alice@example.com");
        updated.setPhone("13900000000");
        updated.setAvatarUrl("/uploads/avatars/7/avatar.png");
        when(userMapper.selectActiveById(7L)).thenReturn(existing, updated);

        UserProfileDTO profile = userService.updateProfile(
                7L,
                new UserProfileUpdateRequest(" new-alice@example.com ", "13900000000", " New Alice ", "/uploads/avatars/7/avatar.png")
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(7L, saved.getId());
        assertEquals("new-alice@example.com", saved.getEmail());
        assertEquals("13900000000", saved.getPhone());
        assertEquals("New Alice", saved.getNickname());
        assertNull(saved.getRealName());
        assertNull(saved.getDepartment());
        assertEquals("/uploads/avatars/7/avatar.png", saved.getAvatarUrl());
        assertEquals("new-alice@example.com", profile.email());
        assertEquals("13900000000", profile.phone());
        assertEquals("New Alice", profile.nickname());
        assertEquals("Alice Real", profile.realName());
        assertEquals("Software", profile.department());
        assertEquals("/uploads/avatars/7/avatar.png", profile.avatarUrl());
    }

    @Test
    void updateProfileRejectsInvalidEmail() {
        when(userMapper.selectActiveById(7L)).thenReturn(activeUser());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateProfile(7L, new UserProfileUpdateRequest("bad-email", "13800000000", "Alice", null))
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    @Test
    void updateProfileRejectsInvalidPhone() {
        when(userMapper.selectActiveById(7L)).thenReturn(activeUser());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateProfile(7L, new UserProfileUpdateRequest("alice@example.com", "12345", "Alice", null))
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    @Test
    void deleteAccountDeletesByPrimaryKey() {
        when(userMapper.selectActiveById(7L)).thenReturn(activeUser());
        when(userMapper.deleteById(7L)).thenReturn(1);

        userService.deleteAccount(7L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals(7L, userCaptor.getValue().getId());
        assertEquals("__deleted_7", userCaptor.getValue().getStudentId());
        verify(userMapper).deleteById(7L);
    }

    @Test
    void getPublicUserReturnsOnlyPublicFields() {
        when(userMapper.selectActiveById(7L)).thenReturn(activeUser());

        UserPublicDTO publicUser = userService.getPublicUser(7L);

        assertEquals(7L, publicUser.userId());
        assertEquals("Alice", publicUser.nickname());
        assertEquals("avatar.png", publicUser.avatarUrl());
        assertEquals("Software", publicUser.department());
        assertEquals(96, publicUser.creditScore());
        assertEquals(CreditService.creditLevel(96), publicUser.creditLevel());
    }

    @Test
    void getHomeReturnsCurrentAggregateSnapshot() {
        when(userMapper.selectActiveById(7L)).thenReturn(activeUser());
        when(creditMapper.countPublishedTasks(7L)).thenReturn(4L);
        when(creditMapper.countCompletedOrdersByUser(7L)).thenReturn(3L);
        when(creditMapper.countReceivedReviews(7L)).thenReturn(2L);
        when(creditMapper.averageReceivedRating(7L)).thenReturn(BigDecimal.valueOf(4.5));

        UserHomeDTO home = userService.getHome(7L);

        assertEquals(7L, home.userId());
        assertEquals("Alice", home.nickname());
        assertEquals(4, home.publishedTaskCount());
        assertEquals(3, home.completedOrderCount());
        assertEquals(2, home.reviewCount());
        assertEquals(BigDecimal.valueOf(4.5), home.averageRating());
    }

    @Test
    void banUserMarksUserBannedAndReturnsResult() {
        when(userMapper.selectActiveById(7L)).thenReturn(activeUser());

        AdminBanResultDTO result = userService.banUser(7L, new AdminBanRequest("violation", 7));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals(7L, userCaptor.getValue().getId());
        assertEquals(2, userCaptor.getValue().getStatus());
        assertEquals(7L, result.userId());
        assertEquals("BANNED", result.status());
        assertEquals("violation", result.reason());
    }

    @Test
    void banUserRejectsMissingUser() {
        when(userMapper.selectActiveById(7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.banUser(7L, new AdminBanRequest("reason", 7))
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    private static User activeUser() {
        User user = new User();
        user.setId(7L);
        user.setStudentId("20260001");
        user.setEmail("alice@example.com");
        user.setPhone("13800000000");
        user.setNickname("Alice");
        user.setRealName("Alice Real");
        user.setDepartment("Software");
        user.setAvatarUrl("avatar.png");
        user.setRole(0);
        user.setCreditScore(96);
        user.setStatus(0);
        user.setIsDeleted(false);
        return user;
    }
}
