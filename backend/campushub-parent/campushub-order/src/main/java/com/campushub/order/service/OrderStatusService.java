package com.campushub.order.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.entity.Order;
import com.campushub.order.event.OrderCompletedEvent;
import com.campushub.order.event.OrderConfirmedEvent;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.service.TaskStatusService;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStatusService {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final TaskStatusService taskStatusService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderStatusService(
            OrderMapper orderMapper,
            OrderService orderService,
            TaskStatusService taskStatusService,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.taskStatusService = taskStatusService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public OrderDetailDTO confirm(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (!Objects.equals(order.getPosterId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发布者可以确认订单");
        }
        transition(order, OrderCodecs.ORDER_STATUS_PENDING, OrderCodecs.ORDER_STATUS_CONFIRMED, "订单已被其他人更新");
        taskStatusService.markInProgress(order.getTaskId());
        applicationEventPublisher.publishEvent(new OrderConfirmedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId()));
        return orderService.getDetail(orderId, currentUserId);
    }

    @Transactional
    public OrderDetailDTO complete(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        transition(order, OrderCodecs.ORDER_STATUS_CONFIRMED, OrderCodecs.ORDER_STATUS_COMPLETED, "订单已被其他人更新");
        taskStatusService.markCompleted(order.getTaskId());
        applicationEventPublisher.publishEvent(new OrderCompletedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId()));
        return orderService.getDetail(orderId, currentUserId);
    }

    @Transactional
    public OrderDetailDTO cancel(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (order.getStatus() == null
                || (order.getStatus() != OrderCodecs.ORDER_STATUS_PENDING
                && order.getStatus() != OrderCodecs.ORDER_STATUS_CONFIRMED)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不允许取消");
        }
        int updated = orderMapper.updateStatusAndVersion(
                order.getId(),
                OrderCodecs.ORDER_STATUS_CANCELLED,
                order.getStatus(),
                order.getVersion()
        );
        if (updated == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单已被其他人更新");
        }
        taskStatusService.reopen(order.getTaskId());
        return orderService.getDetail(orderId, currentUserId);
    }

    private void transition(Order order, int from, int to, String conflictMessage) {
        if (order.getStatus() == null || order.getStatus() != from) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不允许执行该操作");
        }
        int updated = orderMapper.updateStatusAndVersion(order.getId(), to, from, order.getVersion());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, conflictMessage);
        }
    }
}
