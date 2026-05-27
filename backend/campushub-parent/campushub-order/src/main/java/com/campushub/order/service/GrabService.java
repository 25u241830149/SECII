package com.campushub.order.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.entity.Order;
import com.campushub.order.event.OrderCreatedEvent;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.entity.Task;
import com.campushub.task.service.TaskCodecs;
import com.campushub.task.service.TaskService;
import com.campushub.task.service.TaskStatusService;
import java.util.Objects;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrabService {

    private final RedissonClient redissonClient;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final TaskService taskService;
    private final TaskStatusService taskStatusService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GrabService(
            RedissonClient redissonClient,
            OrderMapper orderMapper,
            OrderService orderService,
            TaskService taskService,
            TaskStatusService taskStatusService,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.redissonClient = redissonClient;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.taskService = taskService;
        this.taskStatusService = taskStatusService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public OrderDetailDTO grab(GrabOrderRequest request, Long helperId) {
        if (request == null || request.taskId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "taskId 不能为空");
        }
        ValidateUtils.requirePositive(request.taskId(), "taskId");

        RLock lock = redissonClient.getLock("campushub:grab:task:" + request.taskId());
        lock.lock();
        try {
            Task task = taskService.requireTask(request.taskId());
            if (Objects.equals(task.getPublisherId(), helperId)) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能接取自己发布的需求");
            }
            if (Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)) {
                return applyForTeamUp(task, helperId);
            }
            if (!Objects.equals(task.getStatus(), TaskCodecs.TASK_STATUS_OPEN)) {
                throw new BusinessException(ErrorCode.CONFLICT, "任务已被抢单或已关闭");
            }
            if (!taskStatusService.lockForGrab(task.getId())) {
                throw new BusinessException(ErrorCode.CONFLICT, "任务已被其他人抢走");
            }

            try {
                Order order = new Order();
                order.setTaskId(task.getId());
                order.setPosterId(task.getPublisherId());
                order.setHelperId(helperId);
                order.setStatus(OrderCodecs.ORDER_STATUS_PENDING);
                order.setVersion(0);
                orderMapper.insertOrder(order);
                applicationEventPublisher.publishEvent(new OrderCreatedEvent(
                        order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId()));
                return orderService.getDetail(order.getId(), helperId);
            } catch (DuplicateKeyException ex) {
                taskStatusService.reopen(task.getId());
                throw new BusinessException(ErrorCode.CONFLICT, "任务已被其他人抢走", ex);
            } catch (RuntimeException ex) {
                taskStatusService.reopen(task.getId());
                throw ex;
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private OrderDetailDTO applyForTeamUp(Task task, Long helperId) {
        if (!Objects.equals(task.getStatus(), TaskCodecs.TASK_STATUS_IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.CONFLICT, "组队需求已结束或已取消");
        }
        if (task.getTeamTotalMembers() != null
                && task.getTeamCurrentMembers() != null
                && task.getTeamCurrentMembers() >= task.getTeamTotalMembers()) {
            throw new BusinessException(ErrorCode.CONFLICT, "组队人数已满");
        }
        Order order = new Order();
        order.setTaskId(task.getId());
        order.setPosterId(task.getPublisherId());
        order.setHelperId(helperId);
        order.setStatus(OrderCodecs.ORDER_STATUS_PENDING);
        order.setVersion(0);
        try {
            orderMapper.insertOrder(order);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "你已提交过加入申请", ex);
        }
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId()));
        return orderService.getDetail(order.getId(), helperId);
    }
}
