package com.campushub.task.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.JwtAuthFilter;
import com.campushub.common.security.JwtTokenProvider;
import com.campushub.common.testing.ContainerizedIntegrationTestSupport;
import com.campushub.common.testing.DatabaseFixtureHelper;
import com.campushub.common.enums.UserRole;
import com.campushub.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TaskFlowIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskFlowIntegrationTest extends ContainerizedIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    private Long publisherId;
    private String token;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registerContainerProperties(registry);
        registerSchemaProperties(registry);
    }

    @BeforeEach
    void prepareData() {
        jdbcTemplate.execute("DELETE FROM t_task_favorite");
        jdbcTemplate.execute("DELETE FROM t_task_comment");
        jdbcTemplate.execute("DELETE FROM t_order");
        jdbcTemplate.execute("DELETE FROM t_task");
        jdbcTemplate.execute("DELETE FROM t_user_verification");
        jdbcTemplate.execute("DELETE FROM u_user WHERE student_id <> 'admin'");
        publisherId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20260001", "Alice");
        token = jwtTokenProvider.generateAccessToken(publisherId, "20260001", UserRole.USER);
    }

    @Test
    void createDetailUpdateAndDeleteTaskThroughHttpFlow() throws Exception {
        String createResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Need pickup",
                                  "description": "Bring parcel back",
                                  "category": "EXPRESS",
                                  "location": "North Gate",
                                  "reward": 8.5,
                                  "longitude": 120.123,
                                  "latitude": 30.456
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).path("data").path("taskId").longValue();

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.publisherId").value(publisherId));

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Need pickup urgently",
                                  "description": "Bring parcel to dorm",
                                  "category": "EXPRESS",
                                  "location": "Dormitory",
                                  "reward": 10.0,
                                  "longitude": 120.123,
                                  "latitude": 30.456
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Need pickup urgently"));

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM t_task WHERE id = ?",
                Integer.class,
                taskId
        );
        assertEquals(4, status);
    }

    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = {
            com.campushub.task.controller.TaskController.class,
            com.campushub.task.service.TaskService.class,
            com.campushub.task.service.TaskQueryService.class,
            com.campushub.task.service.TaskFavoriteService.class,
            com.campushub.common.audit.SensitiveWordFilter.class,
            GlobalExceptionHandler.class,
            JwtAuthFilter.class,
            JwtTokenProvider.class,
            com.campushub.common.config.SecurityConfig.class
    })
    @MapperScan("com.campushub.task.mapper")
    static class TestApplication {
    }
}
