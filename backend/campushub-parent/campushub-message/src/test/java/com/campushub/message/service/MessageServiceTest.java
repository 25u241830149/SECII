package com.campushub.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.message.dto.MessageDTO;
import com.campushub.message.entity.Message;
import com.campushub.message.mapper.MessageMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    @Test
    void listBuildsPageResponseWithDecodedTypeFilter() {
        MessageDTO dto = new MessageDTO(
                11L,
                7L,
                "TASK",
                "Task updated",
                "body",
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
        when(messageMapper.selectMessages(eq(7L), eq(MessageCodecs.TYPE_TASK), eq(true), eq(10), eq(5)))
                .thenReturn(List.of(dto));
        when(messageMapper.countMessages(eq(7L), eq(MessageCodecs.TYPE_TASK), eq(true))).thenReturn(1L);

        PageResponse<MessageDTO> page = messageService.list(7L, "task", true, 3, 5);

        assertEquals(1L, page.total());
        assertEquals(3, page.page());
        assertEquals(5, page.size());
        assertEquals(dto, page.records().get(0));
    }

    @Test
    void markReadRejectsUnknownMessage() {
        when(messageMapper.markRead(11L, 7L)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> messageService.markRead(11L, 7L)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void notifyUserTrimsAndTruncatesPayload() {
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);

        messageService.notifyUser(7L, MessageCodecs.TYPE_SYSTEM, "  " + "x".repeat(130) + "  ", "  hello  ");

        verify(messageMapper).insertMessage(captor.capture());
        Message message = captor.getValue();
        assertEquals(7L, message.getReceiverId());
        assertEquals(MessageCodecs.TYPE_SYSTEM, message.getType());
        assertEquals(128, message.getTitle().length());
        assertEquals("hello", message.getContent());
        assertFalse(message.getRead());
    }

    @Test
    void notifyIfDifferentSkipsSelfNotification() {
        messageService.notifyIfDifferent(7L, 7L, MessageCodecs.TYPE_REPORT, "Title", "Body");

        verifyNoInteractions(messageMapper);
    }
}
