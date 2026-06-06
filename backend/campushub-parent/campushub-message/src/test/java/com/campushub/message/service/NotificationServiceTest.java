package com.campushub.message.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.event.OrderConfirmedEvent;
import com.campushub.order.event.OrderCreatedEvent;
import com.campushub.review.event.ReviewCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void onOrderCreatedNotifiesBothSidesForNormalOrder() {
        notificationService.onOrderCreated(new OrderCreatedEvent(31L, 11L, 7L, 9L, "Need pickup", false));

        verify(messageService).notifyUser(eq(7L), eq(MessageCodecs.TYPE_ORDER), anyString(), anyString());
        verify(messageService).notifyUser(eq(9L), eq(MessageCodecs.TYPE_ORDER), anyString(), anyString());
    }

    @Test
    void onOrderConfirmedTeamUpOnlyNotifiesHelper() {
        notificationService.onOrderConfirmed(new OrderConfirmedEvent(31L, 11L, 7L, 9L, "Team up", true));

        verify(messageService).notifyUser(eq(9L), eq(MessageCodecs.TYPE_ORDER), anyString(), anyString());
        verify(messageService, never()).notifyUser(eq(7L), eq(MessageCodecs.TYPE_ORDER), anyString(), anyString());
    }

    @Test
    void onOrderCancelledUsesNotifyIfDifferent() {
        notificationService.onOrderCancelled(new OrderCancelledEvent(31L, 11L, 7L, 9L, "Need pickup"));

        verify(messageService).notifyIfDifferent(eq(9L), eq(7L), eq(MessageCodecs.TYPE_ORDER), anyString(), anyString());
    }

    @Test
    void onReviewCreatedUsesNotifyIfDifferent() {
        notificationService.onReviewCreated(new ReviewCreatedEvent(41L, 31L, 7L, 9L, 5));

        verify(messageService).notifyIfDifferent(eq(9L), eq(7L), eq(MessageCodecs.TYPE_REVIEW), anyString(), anyString());
    }
}
