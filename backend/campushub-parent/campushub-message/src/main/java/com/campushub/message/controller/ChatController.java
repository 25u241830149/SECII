package com.campushub.message.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import com.campushub.message.service.ChatService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/{orderId}/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ApiResponse<List<ChatMessageDTO>> list(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(chatService.list(orderId, currentUserId));
    }

    @PostMapping
    public ApiResponse<ChatMessageDTO> send(@PathVariable Long orderId, @RequestBody ChatSendRequest request) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(chatService.send(orderId, currentUserId, request));
    }
}
