package com.campushub.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.dto.ChatSendRequest;
import com.campushub.message.entity.ChatMessage;
import com.campushub.message.mapper.ChatMessageMapper;
import com.campushub.order.entity.Order;
import com.campushub.order.service.OrderService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void sendRejectsCancelledOrder() {
        when(orderService.requireAccessibleOrder(31L, 7L)).thenReturn(order(31L, 7L, 9L, 3));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatService.send(31L, 7L, new ChatSendRequest("hello"))
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void sendPersistsMessageAndReturnsInsertedDetail() {
        when(orderService.requireAccessibleOrder(31L, 7L)).thenReturn(order(31L, 7L, 9L, 1));
        when(chatMessageMapper.insertChatMessage(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(41L);
            return 1;
        });
        ChatMessageDTO detail = new ChatMessageDTO(
                41L,
                31L,
                7L,
                "Poster",
                null,
                9L,
                "Helper",
                null,
                "hello",
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
        when(chatMessageMapper.selectChatMessages(31L)).thenReturn(List.of(detail));

        ChatMessageDTO result = chatService.send(31L, 7L, new ChatSendRequest("  hello  "));

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageMapper).insertChatMessage(captor.capture());
        assertEquals(9L, captor.getValue().getReceiverId());
        assertEquals("hello", captor.getValue().getContent());
        assertEquals(detail, result);
    }

    @Test
    void listMarksMessagesReadForCurrentUser() {
        when(orderService.requireAccessibleOrder(31L, 7L)).thenReturn(order(31L, 7L, 9L, 1));
        List<ChatMessageDTO> messages = List.of(new ChatMessageDTO(
                41L,
                31L,
                9L,
                "Helper",
                null,
                7L,
                "Poster",
                null,
                "hello",
                true,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        ));
        when(chatMessageMapper.selectChatMessages(31L)).thenReturn(messages);

        List<ChatMessageDTO> result = chatService.list(31L, 7L);

        verify(chatMessageMapper).markOrderMessagesRead(31L, 7L);
        assertEquals(messages, result);
    }

    private static Order order(Long orderId, Long posterId, Long helperId, Integer status) {
        Order order = new Order();
        order.setId(orderId);
        order.setPosterId(posterId);
        order.setHelperId(helperId);
        order.setStatus(status);
        return order;
    }
}
