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
import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import com.campushub.message.service.ChatService;
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
class ChatControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listRequiresAuthenticationContext() throws Exception {
        mockMvc.perform(get("/api/orders/31/chat"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(chatService);
    }

    @Test
    void listUsesCurrentUser() throws Exception {
        authenticate(7L, UserRole.USER);
        when(chatService.list(31L, 7L)).thenReturn(List.of(chatMessage(41L, 31L, 7L, 9L, "hello")));

        mockMvc.perform(get("/api/orders/31/chat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].messageId").value(41L))
                .andExpect(jsonPath("$.data[0].content").value("hello"));
    }

    @Test
    void sendUsesCurrentUserAndWrapsDetail() throws Exception {
        authenticate(7L, UserRole.USER);
        ChatSendRequest request = new ChatSendRequest("hello");
        when(chatService.send(eq(31L), eq(7L), eq(request))).thenReturn(chatMessage(41L, 31L, 7L, 9L, "hello"));

        mockMvc.perform(post("/api/orders/31/chat")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.senderId").value(7L))
                .andExpect(jsonPath("$.data.receiverId").value(9L));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static ChatMessageDTO chatMessage(Long messageId, Long orderId, Long senderId, Long receiverId, String content) {
        return new ChatMessageDTO(
                messageId,
                orderId,
                senderId,
                "Sender",
                null,
                receiverId,
                "Receiver",
                null,
                content,
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
