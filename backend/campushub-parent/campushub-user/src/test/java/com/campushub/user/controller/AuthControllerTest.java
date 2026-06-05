package com.campushub.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.response.ApiResponse;
import com.campushub.user.dto.LoginRequest;
import com.campushub.user.dto.LoginResponseDTO;
import com.campushub.user.dto.RegisterRequest;
import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerDelegatesToServiceAndWrapsSuccess() {
        RegisterRequest request = new RegisterRequest(
                "20260001",
                "CampusHub123",
                "Alice",
                "张三",
                "软件学院",
                "student-cards/20260001.jpg"
        );

        ApiResponse<Void> response = authController.register(request);

        verify(authService).register(request);
        assertEquals(200, response.code());
        assertNull(response.data());
    }

    @Test
    void loginDelegatesToServiceAndWrapsTokenResponse() {
        LoginRequest request = new LoginRequest("20260001", "CampusHub123");
        LoginResponseDTO loginResponse = new LoginResponseDTO(
                "jwt-token",
                new UserInfoDTO(7L, "20260001", "alice@example.com", "13800000000", "Alice", "张三", "软件学院", "avatar.png", "USER", 100, "PENDING")
        );
        when(authService.login(request)).thenReturn(loginResponse);

        ApiResponse<LoginResponseDTO> response = authController.login(request);

        assertEquals(200, response.code());
        assertEquals("jwt-token", response.data().token());
        assertEquals(7L, response.data().user().userId());
    }
}
