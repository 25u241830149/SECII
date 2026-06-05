package com.campushub.user.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilsBoundaryTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireCurrentUserAllowsOwner() {
        authenticate(7L, UserRole.USER);

        SecurityUtils.requireCurrentUser(7L);

        assertEquals(7L, SecurityUtils.getRequiredCurrentUserId());
    }

    @Test
    void requireCurrentUserAllowsAdminForAnotherUser() {
        authenticate(1L, UserRole.ADMIN);

        SecurityUtils.requireCurrentUser(7L);

        assertEquals(1L, SecurityUtils.getRequiredCurrentUserId());
    }

    @Test
    void requireCurrentUserRejectsDifferentNonAdminUser() {
        authenticate(8L, UserRole.USER);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> SecurityUtils.requireCurrentUser(7L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    @Test
    void requireAdminRejectsMissingAuthentication() {
        BusinessException exception = assertThrows(BusinessException.class, SecurityUtils::requireAdmin);

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityUtils.CurrentUser currentUser = new SecurityUtils.CurrentUser(userId, "user-" + userId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, java.util.List.of(() -> "ROLE_" + role.name()))
        );
    }
}
