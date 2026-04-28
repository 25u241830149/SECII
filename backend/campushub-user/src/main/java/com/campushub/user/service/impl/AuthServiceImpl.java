package com.campushub.user.service.impl;

import com.campushub.user.dto.LoginRequest;
import com.campushub.user.dto.LoginResponseDTO;
import com.campushub.user.dto.RegisterRequest;
import com.campushub.user.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public void register(RegisterRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public LoginResponseDTO login(LoginRequest request) {
        return LoginResponseDTO.builder()
                .username(request.getUsername())
                .token("scaffold-token")
                .role("STUDENT")
                .build();
    }
}