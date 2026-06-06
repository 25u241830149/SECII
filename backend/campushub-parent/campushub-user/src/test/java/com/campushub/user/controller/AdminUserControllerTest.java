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
import com.campushub.user.dto.AdminBanRequest;
import com.campushub.user.dto.AdminBanResultDTO;
import com.campushub.user.dto.AdminVerifyRequest;
import com.campushub.user.dto.AdminVerifyResultDTO;
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
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private AdminUserController adminUserController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void banRequiresAdminAndWrapsResult() {
        authenticate(1L, UserRole.ADMIN);
        AdminBanRequest request = new AdminBanRequest("恶意违规", 7);
        AdminBanResultDTO result = new AdminBanResultDTO(7L, "BANNED", "恶意违规");
        when(userService.banUser(7L, request)).thenReturn(result);

        ApiResponse<AdminBanResultDTO> response = adminUserController.banUser(7L, request);

        assertEquals("BANNED", response.data().status());
        verify(userService).banUser(7L, request);
    }

    @Test
    void verifyRequiresAdminAndWrapsResult() {
        authenticate(1L, UserRole.ADMIN);
        AdminVerifyRequest request = new AdminVerifyRequest(true, "材料真实");
        AdminVerifyResultDTO result = new AdminVerifyResultDTO(7L, "APPROVED", "材料真实");
        when(verificationService.review(7L, 1L, request)).thenReturn(result);

        ApiResponse<AdminVerifyResultDTO> response = adminUserController.verifyUser(7L, request);

        assertEquals("APPROVED", response.data().verificationStatus());
        verify(verificationService).review(7L, 1L, request);
    }

    @Test
    void banRejectsNonAdminUser() {
        authenticate(7L, UserRole.USER);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminUserController.banUser(8L, new AdminBanRequest("违规", null))
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        verifyNoInteractions(userService, verificationService);
    }

    @Test
    void verifyRejectsNonAdminUserBeforeServiceCall() {
        authenticate(7L, UserRole.USER);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminUserController.verifyUser(8L, new AdminVerifyRequest(true, "looks valid"))
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        verifyNoInteractions(userService, verificationService);
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityUtils.CurrentUser currentUser = new SecurityUtils.CurrentUser(userId, "admin", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, java.util.List.of(() -> "ROLE_" + role.name()))
        );
    }
}
