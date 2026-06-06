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
import com.campushub.user.service.UserService;
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
    private final UserService userService;

    public GrabService(
            RedissonClient redissonClient,
            OrderMapper orderMapper,
            OrderService orderService,
            TaskService taskService,
            TaskStatusService taskStatusService,
            ApplicationEventPublisher applicationEventPublisher,
            UserService userService
    ) {
        this.redissonClient = redissonClient;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.taskService = taskService;
        this.taskStatusService = taskStatusService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userService = userService;
    }

    @Transactional
    public OrderDetailDTO grab(GrabOrderRequest request, Long helperId) {
        userService.requireOperableUser(helperId);
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
            if (orderMapper.countOrdersByTaskAndHelper(task.getId(), helperId) > 0) {
                String message = Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)
                        ? "你已申请过加入该队伍，无法重复申请"
                        : "你已申请过该需求，发布者拒绝或订单取消后无法再次接单";
                throw new BusinessException(ErrorCode.CONFLICT, message);
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
                        order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(),
                        task.getTitle(), false));
                return orderService.getDetail(order.getId(), helperId);
            } catch (DuplicateKeyException ex) {
                taskStatusService.reopen(task.getId());
                throw new BusinessException(ErrorCode.CONFLICT, "你已申请过该需求，或任务已被其他人抢走", ex);
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
            throw new BusinessException(ErrorCode.CONFLICT, "你已申请过加入该队伍，无法重复申请", ex);
        }
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(
                order.getId(), order.getTaskId(), order.getPosterId(), order.getHelperId(),
                task.getTitle(), true));
        return orderService.getDetail(order.getId(), helperId);
    }
}
