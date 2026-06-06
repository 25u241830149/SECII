package com.campushub.report.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.SecurityUtils;
import com.campushub.report.dto.AdminDashboardStatsDTO;
import com.campushub.report.service.AdminStatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class AdminStatsControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AdminStatsService adminStatsService;

    @InjectMocks
    private AdminStatsController adminStatsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminStatsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dashboardRejectsNonAdminUser() throws Exception {
        authenticate(7L, UserRole.USER);

        mockMvc.perform(get("/api/admin/stats/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        verifyNoInteractions(adminStatsService);
    }

    @Test
    void dashboardWrapsAdminSnapshot() throws Exception {
        authenticate(1L, UserRole.ADMIN);
        when(adminStatsService.dashboard()).thenReturn(dashboardStats());

        mockMvc.perform(get("/api/admin/stats/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.averageRating").value(4.60));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static AdminDashboardStatsDTO dashboardStats() {
        return new AdminDashboardStatsDTO(
                100L, 4L, 2L, 20L, 8L, 50L, 30L, 18L, 3L, 12L, 7L, 45L, new BigDecimal("4.60")
        );
    }
}
