package com.campushub.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class GrabServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskStatusService taskStatusService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private UserService userService;

    @InjectMocks
    private GrabService grabService;

    @Test
    void grabRejectsMissingTaskId() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(null), 7L)
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    @Test
    void grabRejectsSelfGrab() {
        prepareLock(11L);
        when(taskService.requireTask(11L)).thenReturn(normalTask(11L, 7L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(11L), 7L)
        );

        assertEquals(ErrorCode.BUSINESS_ERROR, exception.getCode());
    }

    @Test
    void grabRejectsClosedNormalTask() {
        prepareLock(11L);
        Task task = normalTask(11L, 7L);
        task.setStatus(TaskCodecs.TASK_STATUS_COMPLETED);
        when(taskService.requireTask(11L)).thenReturn(task);
        when(orderMapper.countOrdersByTaskAndHelper(11L, 9L)).thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(11L), 9L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
        verify(taskStatusService, never()).lockForGrab(11L);
    }

    @Test
    void grabRejectsWhenOptimisticTaskLockFails() {
        prepareLock(11L);
        when(taskService.requireTask(11L)).thenReturn(normalTask(11L, 7L));
        when(orderMapper.countOrdersByTaskAndHelper(11L, 9L)).thenReturn(0L);
        when(taskStatusService.lockForGrab(11L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(11L), 9L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void grabReopensTaskWhenInsertHitsDuplicateKey() {
        prepareLock(11L);
        when(taskService.requireTask(11L)).thenReturn(normalTask(11L, 7L));
        when(orderMapper.countOrdersByTaskAndHelper(11L, 9L)).thenReturn(0L);
        when(taskStatusService.lockForGrab(11L)).thenReturn(true);
        when(orderMapper.insertOrder(any(Order.class))).thenThrow(new DuplicateKeyException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(11L), 9L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
        verify(taskStatusService).reopen(11L);
    }

    @Test
    void grabCreatesNormalOrderAndPublishesEvent() {
        prepareLock(11L);
        when(taskService.requireTask(11L)).thenReturn(normalTask(11L, 7L));
        when(orderMapper.countOrdersByTaskAndHelper(11L, 9L)).thenReturn(0L);
        when(taskStatusService.lockForGrab(11L)).thenReturn(true);
        when(orderMapper.insertOrder(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(31L);
            return 1;
        });
        OrderDetailDTO detail = detail(31L, "PENDING");
        when(orderService.getDetail(31L, 9L)).thenReturn(detail);

        OrderDetailDTO result = grabService.grab(new GrabOrderRequest(11L), 9L);

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(false, eventCaptor.getValue().teamUp());
        assertEquals(detail, result);
    }

    @Test
    void teamUpGrabRejectsFullTeam() {
        prepareLock(21L);
        when(taskService.requireTask(21L)).thenReturn(teamTask(21L, 7L, TaskCodecs.TASK_STATUS_IN_PROGRESS, 3, 3));
        when(orderMapper.countOrdersByTaskAndHelper(21L, 9L)).thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(21L), 9L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void teamUpGrabCreatesApplicationWithoutTaskLocking() {
        prepareLock(21L);
        when(taskService.requireTask(21L)).thenReturn(teamTask(21L, 7L, TaskCodecs.TASK_STATUS_IN_PROGRESS, 4, 2));
        when(orderMapper.countOrdersByTaskAndHelper(21L, 9L)).thenReturn(0L);
        when(orderMapper.insertOrder(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(41L);
            return 1;
        });
        OrderDetailDTO detail = detail(41L, "PENDING");
        when(orderService.getDetail(41L, 9L)).thenReturn(detail);

        OrderDetailDTO result = grabService.grab(new GrabOrderRequest(21L), 9L);

        verify(taskStatusService, never()).lockForGrab(21L);
        assertEquals(detail, result);
    }

    private void prepareLock(Long taskId) {
        when(redissonClient.getLock("campushub:grab:task:" + taskId)).thenReturn(lock);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
    }

    private static Task normalTask(Long taskId, Long publisherId) {
        Task task = new Task();
        task.setId(taskId);
        task.setPublisherId(publisherId);
        task.setTitle("Need pickup");
        task.setCategory(TaskCodecs.TASK_CATEGORY_EXPRESS);
        task.setStatus(TaskCodecs.TASK_STATUS_OPEN);
        return task;
    }

    private static Task teamTask(Long taskId, Long publisherId, int status, int total, int current) {
        Task task = new Task();
        task.setId(taskId);
        task.setPublisherId(publisherId);
        task.setTitle("Team up");
        task.setCategory(TaskCodecs.TASK_CATEGORY_TEAM_UP);
        task.setStatus(status);
        task.setTeamTotalMembers(total);
        task.setTeamCurrentMembers(current);
        return task;
    }

    private static OrderDetailDTO detail(Long orderId, String status) {
        return new OrderDetailDTO(
                orderId, 11L, "Need pickup", "desc", "EXPRESS", "OPEN", "Dorm", BigDecimal.TEN,
                status, false, null, null, 7L, "Poster", null, 90, 9L, "Helper", null, 90,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
