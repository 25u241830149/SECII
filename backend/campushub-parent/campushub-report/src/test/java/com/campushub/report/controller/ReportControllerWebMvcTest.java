package com.campushub.report.controller;

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
import com.campushub.report.dto.ReportCreateRequest;
import com.campushub.report.dto.ReportDTO;
import com.campushub.report.dto.ReportHandleRequest;
import com.campushub.report.service.ReportService;
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
class ReportControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRequiresAuthenticationContext() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ReportCreateRequest("USER", 9L, null, "reason"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(reportService);
    }

    @Test
    void listRejectsNonAdminUser() throws Exception {
        authenticate(7L, UserRole.USER);

        mockMvc.perform(get("/api/admin/reports"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        verifyNoInteractions(reportService);
    }

    @Test
    void handleUsesAdminCurrentUser() throws Exception {
        authenticate(1L, UserRole.ADMIN);
        ReportHandleRequest request = new ReportHandleRequest("HANDLED", "resolved");
        when(reportService.handle(eq(31L), eq(1L), eq(request))).thenReturn(reportDetail(31L, "HANDLED"));

        mockMvc.perform(post("/api/admin/reports/31/handle")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(31L))
                .andExpect(jsonPath("$.data.status").value("HANDLED"));
    }

    @Test
    void createUsesCurrentUserAndWrapsResponse() throws Exception {
        authenticate(7L, UserRole.USER);
        ReportCreateRequest request = new ReportCreateRequest("USER", 9L, null, "reason");
        when(reportService.create(eq(7L), eq(request))).thenReturn(reportDetail(31L, "PENDING"));

        mockMvc.perform(post("/api/reports")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(31L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static ReportDTO reportDetail(Long reportId, String status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-05T12:00:00+08:00");
        return new ReportDTO(
                reportId,
                7L,
                "Reporter",
                9L,
                "Target",
                "HANDLED".equals(status) ? 1L : null,
                "HANDLED".equals(status) ? "Admin" : null,
                null,
                null,
                null,
                null,
                "USER",
                9L,
                "reason",
                status,
                "HANDLED".equals(status) ? "resolved" : null,
                now,
                now
        );
    }
}
