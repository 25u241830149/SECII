package com.campushub.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.SecurityUtils;
import com.campushub.user.dto.UploadResultDTO;
import com.campushub.user.service.UploadService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UploadControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private UploadController uploadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(uploadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadAvatarSucceedsForAuthenticatedUser() throws Exception {
        authenticate(7L);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        when(uploadService.uploadAvatar(eq(7L), any())).thenReturn(
                new UploadResultDTO("/uploads/avatars/7/a.png", "avatar.png", "image/png", 3)
        );

        mockMvc.perform(multipart("/api/upload/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileUrl").value("/uploads/avatars/7/a.png"));
    }

    @Test
    void uploadAvatarRejectsUnsupportedFileType() throws Exception {
        authenticate(7L);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.exe", "application/octet-stream", new byte[]{1});
        when(uploadService.uploadAvatar(eq(7L), any()))
                .thenThrow(new BusinessException(400, "unsupported"));

        mockMvc.perform(multipart("/api/upload/avatar").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("unsupported"));
    }

    @Test
    void uploadAvatarRejectsOversizedFile() throws Exception {
        authenticate(7L);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        when(uploadService.uploadAvatar(eq(7L), any()))
                .thenThrow(new BusinessException(400, "too large"));

        mockMvc.perform(multipart("/api/upload/avatar").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("too large"));
    }

    @Test
    void uploadAvatarRejectsAnonymousRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/upload/avatar").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(uploadService);
    }

    private static void authenticate(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, UserRole.USER),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }
}
