package com.campushub.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.user.dto.CreditDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserProfileUpdateRequest;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.dto.VerificationStatusDTO;
import com.campushub.user.dto.VerificationSubmitRequest;
import com.campushub.user.service.CreditService;
import com.campushub.user.service.UserService;
import com.campushub.user.service.VerificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CreditService creditService;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private UserController userController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfileRequiresCurrentUserAndWrapsProfile() {
        authenticate(7L, UserRole.USER);
        UserProfileDTO profile = new UserProfileDTO(7L, "20260001", "alice@example.com", "13800000000", "Alice", "张三", "软件学院", null, "USER", 100, "PENDING", null, null);
        when(userService.getProfile(7L)).thenReturn(profile);

        ApiResponse<UserProfileDTO> response = userController.getProfile(7L);

        assertEquals(200, response.code());
        assertEquals("Alice", response.data().nickname());
        verify(userService).getProfile(7L);
    }

    @Test
    void getProfileAllowsAdminToReadAnotherUser() {
        authenticate(1L, UserRole.ADMIN);
        UserProfileDTO profile = new UserProfileDTO(7L, "20260001", "alice@example.com", "13800000000", "Alice", "张三", "软件学院", null, "USER", 100, "PENDING", null, null);
        when(userService.getProfile(7L)).thenReturn(profile);

        ApiResponse<UserProfileDTO> response = userController.getProfile(7L);

        assertEquals(7L, response.data().userId());
        verify(userService).getProfile(7L);
    }

    @Test
    void getProfileRejectsDifferentNonAdminUserBeforeServiceCall() {
        authenticate(8L, UserRole.USER);

        BusinessException exception = assertThrows(BusinessException.class, () -> userController.getProfile(7L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        verifyNoInteractions(userService, creditService, verificationService);
    }

    @Test
    void updateProfileRequiresCurrentUserAndWrapsUpdatedProfile() {
        authenticate(7L, UserRole.USER);
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("alice@example.com", "13800000000", "New Alice", "/uploads/avatars/7/avatar.png");
        UserProfileDTO profile = new UserProfileDTO(7L, "20260001", "alice@example.com", "13800000000", "New Alice", "张三", "软件学院", "/uploads/avatars/7/avatar.png", "USER", 100, "PENDING", null, null);
        when(userService.updateProfile(7L, request)).thenReturn(profile);

        ApiResponse<UserProfileDTO> response = userController.updateProfile(7L, request);

        assertEquals("New Alice", response.data().nickname());
        verify(userService).updateProfile(7L, request);
    }

    @Test
    void getCreditRequiresCurrentUserAndWrapsCredit() {
        authenticate(7L, UserRole.USER);
        when(creditService.getCredit(7L)).thenReturn(new CreditDTO(95, "诚信学生", 0.0, 0.0, null, null, null, null, null, null));

        ApiResponse<CreditDTO> response = userController.getCredit(7L);

        assertEquals("诚信学生", response.data().creditLevel());
        verify(creditService).getCredit(7L);
    }

    @Test
    void getPublicUserDoesNotRequireAuthentication() {
        UserPublicDTO publicUser = new UserPublicDTO(7L, "Alice", "avatar.png", null, 95, "诚信学生");
        when(userService.getPublicUser(7L)).thenReturn(publicUser);

        ApiResponse<UserPublicDTO> response = userController.getPublicUser(7L);

        assertEquals(7L, response.data().userId());
        verify(userService).getPublicUser(7L);
    }

    @Test
    void getHomeDoesNotRequireAuthentication() {
        UserHomeDTO home = new UserHomeDTO(7L, "Alice", "avatar.png", 95, "诚信学生", 0, 0, 0, null);
        when(userService.getHome(7L)).thenReturn(home);

        ApiResponse<UserHomeDTO> response = userController.getHome(7L);

        assertEquals(0, response.data().publishedTaskCount());
        verify(userService).getHome(7L);
    }

    @Test
    void submitVerificationRequiresCurrentUserAndWrapsStatus() {
        authenticate(7L, UserRole.USER);
        VerificationSubmitRequest request = new VerificationSubmitRequest(
                7L,
                "张三",
                "20260001",
                "软件学院",
                "student-cards/7.jpg"
        );
        when(verificationService.submit(request)).thenReturn(new VerificationStatusDTO("PENDING"));

        ApiResponse<VerificationStatusDTO> response = userController.submitVerification(request);

        assertEquals("PENDING", response.data().verificationStatus());
        verify(verificationService).submit(request);
    }

    @Test
    void submitVerificationRejectsDifferentNonAdminUserBeforeServiceCall() {
        authenticate(8L, UserRole.USER);
        VerificationSubmitRequest request = new VerificationSubmitRequest(
                7L,
                "Alice Real",
                "20260001",
                "Software College",
                "student-cards/7.jpg"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userController.submitVerification(request)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        verifyNoInteractions(userService, creditService, verificationService);
    }

    @Test
    void deleteAccountRequiresCurrentUserAndWrapsSuccess() {
        authenticate(7L, UserRole.USER);

        ApiResponse<Void> response = userController.deleteAccount();

        assertEquals(200, response.code());
        verify(userService).deleteAccount(7L);
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityUtils.CurrentUser currentUser = new SecurityUtils.CurrentUser(userId, "20260001", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.role().equals(UserRole.ADMIN)
                        ? java.util.List.of(() -> "ROLE_ADMIN")
                        : java.util.List.of(() -> "ROLE_USER"))
        );
    }
}
