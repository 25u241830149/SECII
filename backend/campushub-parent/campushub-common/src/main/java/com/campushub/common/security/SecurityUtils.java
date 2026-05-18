package com.campushub.common.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Authentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    public static Optional<CurrentUser> getCurrentUser() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(CurrentUser.class::isInstance)
                .map(CurrentUser.class::cast);
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(CurrentUser::userId);
    }

    public static Long getRequiredCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_MESSAGE));
    }

    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(CurrentUser::username);
    }

    public static Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(CurrentUser::role);
    }

    public static boolean isCurrentUser(Long userId) {
        return userId != null && getCurrentUserId()
                .map(userId::equals)
                .orElse(false);
    }

    public static boolean isAdmin() {
        return getCurrentUserRole()
                .map(UserRole.ADMIN::equals)
                .orElse(false);
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN_MESSAGE);
        }
    }

    public static void requireCurrentUser(Long userId) {
        if (!isCurrentUser(userId) && !isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN_MESSAGE);
        }
    }

    public record CurrentUser(Long userId, String username, UserRole role) {
    }
}
