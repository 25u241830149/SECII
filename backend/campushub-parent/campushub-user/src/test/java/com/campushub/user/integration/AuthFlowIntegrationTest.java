package com.campushub.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.JwtAuthFilter;
import com.campushub.common.security.JwtTokenProvider;
import com.campushub.common.testing.ContainerizedIntegrationTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AuthFlowIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest extends ContainerizedIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registerContainerProperties(registry);
        registerSchemaProperties(registry);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM t_user_verification");
        jdbcTemplate.execute("DELETE FROM u_user WHERE student_id <> 'admin'");
    }

    @Test
    void registerPersistsUserAndPendingVerification() throws Exception {
        String body = """
                {
                  "studentId": "20260001",
                  "password": "CampusHub123",
                  "nickname": "Alice",
                  "realName": "张三",
                  "department": "Software",
                  "studentCardImage": "student-cards/20260001.jpg"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Long userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM u_user WHERE student_id = '20260001'",
                Long.class
        );
        Integer status = jdbcTemplate.queryForObject(
                "SELECT v.status FROM t_user_verification v JOIN u_user u ON v.user_id = u.id WHERE u.student_id = '20260001'",
                Integer.class
        );
        assertEquals(1L, userCount);
        assertEquals(0, status);
    }

    @Test
    void loginReturnsJwtAndUserPayload() throws Exception {
        registerPersistsUserAndPendingVerification();

        String response = mockMvc.perform(post("/api/user/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"studentId":"20260001","password":"CampusHub123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.user.studentId").value("20260001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payload = objectMapper.readTree(response);
        assertNotNull(payload.path("data").path("token").textValue());
    }

    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = {
            com.campushub.user.controller.AuthController.class,
            com.campushub.user.service.AuthService.class,
            GlobalExceptionHandler.class,
            JwtAuthFilter.class,
            JwtTokenProvider.class,
            com.campushub.common.config.SecurityConfig.class
    })
    @MapperScan("com.campushub.user.mapper")
    static class TestApplication {
    }
}
