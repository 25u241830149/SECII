package com.campushub.user.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.user.dto.LoginRequest;
import com.campushub.user.dto.LoginResponseDTO;
import com.campushub.user.dto.RegisterRequest;
import com.campushub.user.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
