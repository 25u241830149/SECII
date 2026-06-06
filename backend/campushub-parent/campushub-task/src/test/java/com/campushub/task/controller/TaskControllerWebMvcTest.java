package com.campushub.task.controller;

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
import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.service.TaskFavoriteService;
import com.campushub.task.service.TaskQueryService;
import com.campushub.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class TaskControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private TaskService taskService;

    @Mock
    private TaskQueryService taskQueryService;

    @Mock
    private TaskFavoriteService taskFavoriteService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
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
        TaskCreateRequest request = new TaskCreateRequest(
                "Need pickup", "desc", "EXPRESS", "Dorm", BigDecimal.ONE,
                null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(taskService);
    }

    @Test
    void createUsesCurrentUserAndWrapsResponse() throws Exception {
        authenticate(7L);
        TaskCreateRequest request = new TaskCreateRequest(
                "Need pickup", "desc", "EXPRESS", "Dorm", BigDecimal.ONE,
                null, null, null, null, null, null, null, null, null
        );
        when(taskService.create(eq(7L), eq(request))).thenReturn(taskDetail());

        mockMvc.perform(post("/api/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value(11L))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    void favoritesRejectMismatchedUser() throws Exception {
        authenticate(7L);

        mockMvc.perform(get("/api/tasks/favorites").param("userId", "8"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        verifyNoInteractions(taskFavoriteService);
    }

    private static void authenticate(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, UserRole.USER),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }

    private static TaskDetailDTO taskDetail() {
        return new TaskDetailDTO(
                11L,
                "Need pickup",
                "desc",
                "EXPRESS",
                "OPEN",
                BigDecimal.ONE,
                "Dorm",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                7L,
                "Alice",
                null,
                "Software",
                90,
                0,
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
