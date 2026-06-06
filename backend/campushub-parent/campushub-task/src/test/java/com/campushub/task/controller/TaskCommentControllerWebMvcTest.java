package com.campushub.task.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.SecurityUtils;
import com.campushub.task.dto.TaskCommentCreateRequest;
import com.campushub.task.dto.TaskCommentDTO;
import com.campushub.task.service.TaskCommentService;
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
class TaskCommentControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private TaskCommentService taskCommentService;

    @InjectMocks
    private TaskCommentController taskCommentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskCommentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listIsPublicAndWrapsComments() throws Exception {
        when(taskCommentService.list(11L, null, "time")).thenReturn(List.of(comment(21L, "hello")));

        mockMvc.perform(get("/api/tasks/11/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].commentId").value(21L));
    }

    @Test
    void createRequiresAuthentication() throws Exception {
        TaskCommentCreateRequest request = new TaskCommentCreateRequest("hello", null, null);

        mockMvc.perform(post("/api/tasks/11/comments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(taskCommentService);
    }

    @Test
    void createUsesCurrentUser() throws Exception {
        authenticate(7L, UserRole.USER);
        TaskCommentCreateRequest request = new TaskCommentCreateRequest("hello", null, null);
        when(taskCommentService.create(eq(11L), eq(7L), eq(request))).thenReturn(List.of(comment(21L, "hello")));

        mockMvc.perform(post("/api/tasks/11/comments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].authorId").value(7L));
    }

    @Test
    void deleteUsesCurrentUser() throws Exception {
        authenticate(7L, UserRole.USER);

        mockMvc.perform(delete("/api/tasks/11/comments/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static TaskCommentDTO comment(Long commentId, String content) {
        return new TaskCommentDTO(
                commentId,
                11L,
                7L,
                "Alice",
                null,
                null,
                null,
                null,
                content,
                0,
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
