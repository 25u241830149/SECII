package com.campushub.message.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.message.dto.MessageDTO;
import com.campushub.message.dto.UnreadCountDTO;
import com.campushub.message.service.MessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<MessageDTO>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unread,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(messageService.list(currentUserId, type, unread, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountDTO> unreadCount() {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(messageService.unreadCount(currentUserId));
    }

    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long messageId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        messageService.markRead(messageId, currentUserId);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        messageService.markAllRead(currentUserId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> delete(@PathVariable Long messageId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        messageService.delete(messageId, currentUserId);
        return ApiResponse.success();
    }
}
