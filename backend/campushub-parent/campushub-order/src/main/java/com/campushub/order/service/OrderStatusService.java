package com.campushub.order.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.entity.Order;
import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.event.OrderCompletedEvent;
import com.campushub.order.event.OrderConfirmedEvent;
import com.campushub.order.event.OrderRejectedEvent;
import com.campushub.order.event.OrderAbandonedEvent;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.entity.Task;
import com.campushub.task.service.TaskCodecs;
import com.campushub.task.service.TaskService;
import com.campushub.task.service.TaskStatusService;
import com.campushub.user.service.CreditService;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStatusService {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final TaskStatusService taskStatusService;
    private final TaskService taskService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CreditService creditService;

    public OrderStatusService(
            OrderMapper orderMapper,
            OrderService orderService,
            TaskStatusService taskStatusService,
            TaskService taskService,
            ApplicationEventPublisher applicationEventPublisher,
            CreditService creditService
    ) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.taskStatusService = taskStatusService;
        this.taskService = taskService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.creditService = creditService;
    }

    @Transactional
    public OrderDetailDTO reject(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (!Objects.equals(order.getPosterId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发布者可以拒绝该申请");
        }
        Task task = taskService.requireTask(order.getTaskId());
        transition(order, OrderCodecs.ORDER_STATUS_PENDING, OrderCodecs.ORDER_STATUS_CANCELLED, "记录已被其他人更新");
        if (!Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)) {
            taskStatusService.reopen(order.getTaskId());
        }
        applicationEventPublisher.publishEvent(new OrderRejectedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(), task.getTitle(),
                Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)));
        return orderService.getDetail(orderId, currentUserId);
    }

    @Transactional
    public OrderDetailDTO abandon(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (!Objects.equals(order.getHelperId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有接单者或申请者可以主动退出");
        }
        if (order.getStatus() == null
                || (order.getStatus() != OrderCodecs.ORDER_STATUS_PENDING
                && order.getStatus() != OrderCodecs.ORDER_STATUS_CONFIRMED)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不允许主动退出");
        }
        Task task = taskService.requireTask(order.getTaskId());
        int previousStatus = order.getStatus();
        transition(order, previousStatus, OrderCodecs.ORDER_STATUS_CANCELLED, "记录已被其他人更新");
        if (Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)) {
            if (previousStatus == OrderCodecs.ORDER_STATUS_CONFIRMED) {
                taskStatusService.decrementTeamCurrentMembers(order.getTaskId());
            }
        } else {
            taskStatusService.reopen(order.getTaskId());
        }
        boolean confirmed = previousStatus == OrderCodecs.ORDER_STATUS_CONFIRMED;
        String reason = Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)
                ? (confirmed ? "退出已加入的组队" : "撤回组队申请")
                : (confirmed ? "确认接单后退出需求" : "接单后撤回申请");
        creditService.adjustCreditScore(currentUserId, confirmed ? -5 : -2, reason, order.getId(), null);
        applicationEventPublisher.publishEvent(new OrderAbandonedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(), task.getTitle(),
                Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)));
        return orderService.getDetail(orderId, currentUserId);
    }

    @Transactional
    public OrderDetailDTO confirm(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (!Objects.equals(order.getPosterId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发布者可以确认该申请");
        }
        Task task = taskService.requireTask(order.getTaskId());
        transition(order, OrderCodecs.ORDER_STATUS_PENDING, OrderCodecs.ORDER_STATUS_CONFIRMED, "记录已被其他人更新");
        if (Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)) {
            if (!taskStatusService.incrementTeamCurrentMembers(order.getTaskId())) {
                throw new BusinessException(ErrorCode.CONFLICT, "组队人数已满或需求已结束");
            }
            Task updatedTask = taskService.requireTask(order.getTaskId());
            if (Objects.equals(updatedTask.getStatus(), TaskCodecs.TASK_STATUS_COMPLETED)) {
                orderMapper.completeConfirmedOrdersByTask(order.getTaskId());
                orderMapper.cancelPendingOrdersByTask(order.getTaskId());
            }
        } else {
            taskStatusService.markInProgress(order.getTaskId());
        }
        applicationEventPublisher.publishEvent(new OrderConfirmedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(), task.getTitle(),
                Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)));
        return orderService.getDetail(orderId, currentUserId);
    }

    @Transactional
    public OrderDetailDTO complete(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (!Objects.equals(order.getPosterId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发布者可以完成订单");
        }
        Task task = taskService.requireTask(order.getTaskId());
        if (Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)) {
            throw new BusinessException(ErrorCode.CONFLICT, "活动组队需求会在满员后自动完成");
        }
        transition(order, OrderCodecs.ORDER_STATUS_CONFIRMED, OrderCodecs.ORDER_STATUS_COMPLETED, "订单已被其他人更新");
        taskStatusService.markCompleted(order.getTaskId());
        creditService.adjustCreditScore(order.getPosterId(), 1, "发布的需求顺利完成", order.getId(), null);
        creditService.adjustCreditScore(order.getHelperId(), 2, "接取的需求顺利完成", order.getId(), null);
        applicationEventPublisher.publishEvent(new OrderCompletedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId()));
        return orderService.getDetail(orderId, currentUserId);
    }

    @Transactional
    public OrderDetailDTO cancel(Long orderId, Long currentUserId) {
        Order order = orderService.requireAccessibleOrder(orderId, currentUserId);
        if (!Objects.equals(order.getPosterId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发布者可以取消需求");
        }
        if (order.getStatus() == null
                || (order.getStatus() != OrderCodecs.ORDER_STATUS_PENDING
                && order.getStatus() != OrderCodecs.ORDER_STATUS_CONFIRMED)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不允许取消");
        }
        Task task = taskService.requireTask(order.getTaskId());
        List<Order> activeOrders = orderMapper.selectActiveOrdersByTask(order.getTaskId());
        orderMapper.cancelOrdersByTask(order.getTaskId());
        taskStatusService.markCancelled(order.getTaskId());
        if (order.getStatus() == OrderCodecs.ORDER_STATUS_CONFIRMED) {
            creditService.adjustCreditScore(currentUserId, -3, "确认接单后取消需求", order.getId(), null);
        }
        activeOrders.forEach(activeOrder -> applicationEventPublisher.publishEvent(new OrderCancelledEvent(
                activeOrder.getId(),
                activeOrder.getTaskId(),
                activeOrder.getPosterId(),
                activeOrder.getHelperId(),
                task.getTitle()
        )));
        return orderService.getDetail(orderId, currentUserId);
    }

    private void transition(Order order, int from, int to, String conflictMessage) {
        if (order.getStatus() == null || order.getStatus() != from) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不允许执行该操作");
        }
        int updated = orderMapper.updateStatusAndVersion(order.getId(), to, from, order.getVersion());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, conflictMessage);
        }
    }
}
