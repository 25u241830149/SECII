package com.campushub.task.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.task.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AdminTaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private AdminTaskController adminTaskController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void offlineRequiresAdminAndWrapsSuccess() {
        authenticate(1L, UserRole.ADMIN);

        ApiResponse<Void> response = adminTaskController.offline(11L);

        assertEquals(200, response.code());
        verify(taskService).adminOffline(11L);
    }

    @Test
    void offlineRejectsNonAdminUser() {
        authenticate(7L, UserRole.USER);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminTaskController.offline(11L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        verifyNoInteractions(taskService);
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityUtils.CurrentUser currentUser = new SecurityUtils.CurrentUser(userId, "admin", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, java.util.List.of(() -> "ROLE_" + role.name()))
        );
    }
}
