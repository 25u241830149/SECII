package com.campushub.user.service;

import com.campushub.user.dto.LoginRequest;
import com.campushub.user.dto.LoginResponseDTO;
import com.campushub.user.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponseDTO login(LoginRequest request);
}