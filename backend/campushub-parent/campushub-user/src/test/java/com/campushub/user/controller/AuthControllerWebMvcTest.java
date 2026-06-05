package com.campushub.user.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.exception.BusinessException;
import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.user.dto.LoginRequest;
import com.campushub.user.dto.LoginResponseDTO;
import com.campushub.user.dto.RegisterRequest;
import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registerAcceptsPublicRequestAndWrapsSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "20260001",
                "CampusHub123",
                "Alice",
                "张三",
                "Software",
                "student-cards/20260001.jpg"
        );

        mockMvc.perform(post("/api/user/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(authService).register(request);
    }

    @Test
    void loginReturnsWrappedTokenPayload() throws Exception {
        LoginRequest request = new LoginRequest("20260001", "CampusHub123");
        when(authService.login(request)).thenReturn(new LoginResponseDTO(
                "jwt-token",
                new UserInfoDTO(7L, "20260001", null, null, "Alice", "张三", "Software", null, "USER", 90, "PENDING")
        ));

        mockMvc.perform(post("/api/user/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.userId").value(7L))
                .andExpect(jsonPath("$.data.user.role").value("USER"));
    }

    @Test
    void registerSurfacesBusinessErrorsThroughGlobalHandler() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "20260001",
                "CampusHub123",
                "Alice",
                "张三",
                "Software",
                "student-cards/20260001.jpg"
        );
        doThrow(new BusinessException(409, "duplicate")).when(authService).register(request);

        mockMvc.perform(post("/api/user/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("duplicate"));
    }
}
