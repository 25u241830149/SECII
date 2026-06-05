package com.campushub.order.service;

import com.campushub.order.entity.Order;
import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.event.TaskCancelledEvent;
import com.campushub.user.service.CreditService;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCancellationListener {

    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CreditService creditService;

    public TaskCancellationListener(
            OrderMapper orderMapper,
            ApplicationEventPublisher applicationEventPublisher,
            CreditService creditService) {
        this.orderMapper = orderMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.creditService = creditService;
    }

    @Transactional
    @EventListener
    public void onTaskCancelled(TaskCancelledEvent event) {
        List<Order> activeOrders = orderMapper.selectActiveOrdersByTask(event.taskId());
        orderMapper.cancelOrdersByTask(event.taskId());
        confirmedOrder(activeOrders).ifPresent(order ->
                creditService.adjustCreditScore(event.publisherId(), -3, "确认接单后取消需求", order.getId(), null));
        activeOrders.forEach(order -> applicationEventPublisher.publishEvent(new OrderCancelledEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(), event.taskTitle())));
    }

    private Optional<Order> confirmedOrder(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() != null && order.getStatus() == OrderCodecs.ORDER_STATUS_CONFIRMED)
                .findFirst();
    }
}
