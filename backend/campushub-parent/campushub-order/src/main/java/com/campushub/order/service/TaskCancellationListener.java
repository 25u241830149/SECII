package com.campushub.order.service;

import com.campushub.order.entity.Order;
import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.event.TaskCancelledEvent;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCancellationListener {

    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TaskCancellationListener(OrderMapper orderMapper, ApplicationEventPublisher applicationEventPublisher) {
        this.orderMapper = orderMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    @EventListener
    public void onTaskCancelled(TaskCancelledEvent event) {
        List<Order> activeOrders = orderMapper.selectActiveOrdersByTask(event.taskId());
        orderMapper.cancelOrdersByTask(event.taskId());
        activeOrders.forEach(order -> applicationEventPublisher.publishEvent(new OrderCancelledEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(), event.taskTitle())));
    }
}
