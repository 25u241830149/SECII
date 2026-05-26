package com.campushub.message.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import com.campushub.message.entity.ChatMessage;
import com.campushub.message.mapper.ChatMessageMapper;
import com.campushub.order.entity.Order;
import com.campushub.order.service.OrderService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private static final int MAX_CONTENT_LENGTH = 1000;

    private final ChatMessageMapper chatMessageMapper;
    private final OrderService orderService;

    public ChatService(ChatMessageMapper chatMessageMapper, OrderService orderService) {
        this.chatMessageMapper = chatMessageMapper;
        this.orderService = orderService;
    }

    @Transactional
    public ChatMessageDTO send(Long orderId, Long senderId, ChatSendRequest request) {
        Order order = orderService.requireAccessibleOrder(orderId, senderId);
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "聊天内容不能为空");
        }
        String content = request.content().trim();
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "聊天内容不能超过 1000 个字符");
        }
        Long receiverId = resolveReceiver(order, senderId);
        ChatMessage message = new ChatMessage();
        message.setOrderId(order.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setRead(false);
        chatMessageMapper.insertChatMessage(message);
        return chatMessageMapper.selectChatMessages(order.getId()).stream()
                .filter(item -> Objects.equals(item.messageId(), message.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "聊天消息不存在"));
    }

    @Transactional
    public List<ChatMessageDTO> list(Long orderId, Long currentUserId) {
        ValidateUtils.requirePositive(orderId, "orderId");
        orderService.requireAccessibleOrder(orderId, currentUserId);
        chatMessageMapper.markOrderMessagesRead(orderId, currentUserId);
        return chatMessageMapper.selectChatMessages(orderId);
    }

    private Long resolveReceiver(Order order, Long senderId) {
        if (Objects.equals(order.getPosterId(), senderId)) {
            return order.getHelperId();
        }
        if (Objects.equals(order.getHelperId(), senderId)) {
            return order.getPosterId();
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN_MESSAGE);
    }
}
