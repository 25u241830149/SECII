package com.campushub.message.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import com.campushub.message.dto.MessageListDTO;
import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.service.ChatService;
import com.campushub.message.service.MessageService;
import com.campushub.message.service.MessageQueryService;
import com.campushub.message.service.NoticeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final MessageQueryService messageQueryService;
    private final NoticeService noticeService;

    @GetMapping
    public ApiResponse<PageResponse<MessageListDTO>> getMessages(@RequestParam Long userId,
                                                                 @RequestParam(defaultValue = "1") Long page,
                                                                 @RequestParam(defaultValue = "10") Long size) {
        return ApiResponse.success(messageQueryService.getMessages(userId, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(@RequestParam Long userId) {
        return ApiResponse.success(messageService.getUnreadCount(userId));
    }

    @PutMapping("/{messageId}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return ApiResponse.success("Message read scaffold ready", null);
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<String> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ApiResponse.success("Message delete scaffold ready", null);
    }

    @PostMapping("/chats")
    public ApiResponse<String> sendChatMessage(@RequestBody ChatSendRequest request) {
        chatService.sendMessage(request);
        return ApiResponse.success("Chat scaffold ready", null);
    }

    @GetMapping("/chats")
    public ApiResponse<List<ChatMessageDTO>> getChatMessages(@RequestParam Long orderId,
                                                             @RequestParam Long userId) {
        return ApiResponse.success(chatService.getChatMessages(orderId, userId));
    }

    @GetMapping("/notices")
    public ApiResponse<List<NoticeDTO>> getNotices(@RequestParam Long userId) {
        return ApiResponse.success(noticeService.listNotices(userId));
    }

    @PutMapping("/notices/{noticeId}/read")
    public ApiResponse<String> markNoticeAsRead(@PathVariable Long noticeId) {
        noticeService.markNoticeAsRead(noticeId);
        return ApiResponse.success("Notice read scaffold ready", null);
    }
}