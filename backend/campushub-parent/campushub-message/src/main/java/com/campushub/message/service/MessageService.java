package com.campushub.message.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.message.dto.MessageDTO;
import com.campushub.message.dto.UnreadCountDTO;
import com.campushub.message.entity.Message;
import com.campushub.message.mapper.MessageMapper;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private static final int MAX_TITLE_LENGTH = 128;

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public PageResponse<MessageDTO> list(Long receiverId, String type, Boolean unread, Integer page, Integer size) {
        ValidateUtils.requirePositive(receiverId, "receiverId");
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        Integer typeCode = type == null || type.isBlank() ? null : MessageCodecs.typeCode(type);
        int offset = (normalizedPage - 1) * normalizedSize;
        var records = messageMapper.selectMessages(receiverId, typeCode, unread, offset, normalizedSize);
        long total = messageMapper.countMessages(receiverId, typeCode, unread);
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }

    public UnreadCountDTO unreadCount(Long receiverId) {
        ValidateUtils.requirePositive(receiverId, "receiverId");
        return new UnreadCountDTO(messageMapper.countUnread(receiverId));
    }

    @Transactional
    public void markRead(Long messageId, Long receiverId) {
        ValidateUtils.requirePositive(messageId, "messageId");
        int updated = messageMapper.markRead(messageId, receiverId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "消息不存在");
        }
    }

    @Transactional
    public void markAllRead(Long receiverId) {
        ValidateUtils.requirePositive(receiverId, "receiverId");
        messageMapper.markAllRead(receiverId);
    }

    @Transactional
    public void delete(Long messageId, Long receiverId) {
        ValidateUtils.requirePositive(messageId, "messageId");
        int deleted = messageMapper.deleteMessage(messageId, receiverId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "消息不存在");
        }
    }

    @Transactional
    public void notifyUser(Long receiverId, int type, String title, String content) {
        if (receiverId == null || receiverId <= 0 || title == null || title.isBlank()) {
            return;
        }
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setType(type);
        message.setTitle(trimToLength(title, MAX_TITLE_LENGTH));
        message.setContent(content == null || content.isBlank() ? null : content.trim());
        message.setRead(false);
        messageMapper.insertMessage(message);
    }

    @Transactional
    public void notifyIfDifferent(Long receiverId, Long actorId, int type, String title, String content) {
        if (!Objects.equals(receiverId, actorId)) {
            notifyUser(receiverId, type, title, content);
        }
    }

    private String trimToLength(String value, int maxLength) {
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
