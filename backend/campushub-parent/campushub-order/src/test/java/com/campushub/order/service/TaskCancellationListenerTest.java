package com.campushub.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.entity.Order;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.event.TaskCancelledEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TaskCancellationListenerTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TaskCancellationListener taskCancellationListener;

    @Test
    void onTaskCancelledCancelsActiveOrdersAndPublishesEvents() {
        Order first = order(31L, 11L, 7L, 9L);
        Order second = order(32L, 11L, 7L, 10L);
        when(orderMapper.selectActiveOrdersByTask(11L)).thenReturn(List.of(first, second));

        taskCancellationListener.onTaskCancelled(new TaskCancelledEvent(11L, 7L, "Need pickup"));

        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(orderMapper).cancelOrdersByTask(11L);
        verify(applicationEventPublisher, org.mockito.Mockito.times(2)).publishEvent(eventCaptor.capture());
        assertEquals(
                List.of(
                        new OrderCancelledEvent(31L, 11L, 7L, 9L, "Need pickup"),
                        new OrderCancelledEvent(32L, 11L, 7L, 10L, "Need pickup")
                ),
                eventCaptor.getAllValues()
        );
    }

    private static Order order(Long orderId, Long taskId, Long posterId, Long helperId) {
        Order order = new Order();
        order.setId(orderId);
        order.setTaskId(taskId);
        order.setPosterId(posterId);
        order.setHelperId(helperId);
        return order;
    }
}
