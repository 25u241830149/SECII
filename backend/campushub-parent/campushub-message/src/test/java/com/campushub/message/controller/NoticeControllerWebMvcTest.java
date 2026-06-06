package com.campushub.message.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.SecurityUtils;
import com.campushub.message.dto.NoticeCreateRequest;
import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.service.NoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class NoticeControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private NoticeService noticeService;

    @InjectMocks
    private NoticeController noticeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(noticeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void latestIsPublic() throws Exception {
        when(noticeService.latest(3)).thenReturn(List.of(noticeDetail(21L, "Launch")));

        mockMvc.perform(get("/api/notices/latest").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].noticeId").value(21L));
    }

    @Test
    void createRejectsNonAdminUser() throws Exception {
        authenticate(7L, UserRole.USER);

        mockMvc.perform(post("/api/admin/notices")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new NoticeCreateRequest("Launch", "Body"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        verifyNoInteractions(noticeService);
    }

    @Test
    void createUsesAdminCurrentUser() throws Exception {
        authenticate(1L, UserRole.ADMIN);
        NoticeCreateRequest request = new NoticeCreateRequest("Launch", "Body");
        when(noticeService.create(eq(1L), eq(request))).thenReturn(noticeDetail(21L, "Launch"));

        mockMvc.perform(post("/api/admin/notices")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeId").value(21L))
                .andExpect(jsonPath("$.data.title").value("Launch"));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static NoticeDTO noticeDetail(Long noticeId, String title) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-05T12:00:00+08:00");
        return new NoticeDTO(noticeId, 1L, "Admin", title, "Body", now, now);
    }
}
