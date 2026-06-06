package com.campushub.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.entity.Order;
import com.campushub.order.event.OrderRejectedEvent;
import com.campushub.order.mapper.OrderMapper;
import com.campushub.task.entity.Task;
import com.campushub.task.service.TaskCodecs;
import com.campushub.task.service.TaskService;
import com.campushub.task.service.TaskStatusService;
import com.campushub.user.service.CreditService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private TaskStatusService taskStatusService;

    @Mock
    private TaskService taskService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CreditService creditService;

    @InjectMocks
    private OrderStatusService orderStatusService;

    @Test
    void rejectReopensNormalTaskAndReturnsFreshDetail() {
        Order order = pendingOrder(41L, 11L, 7L, 9L);
        Task task = normalTask(11L, 7L);
        OrderDetailDTO detail = detail(order.getId(), "CANCELLED");
        when(orderService.requireAccessibleOrder(41L, 7L)).thenReturn(order);
        when(taskService.requireTask(11L)).thenReturn(task);
        when(orderMapper.updateStatusAndVersion(41L, 3, 0, 0)).thenReturn(1);
        when(orderService.getDetail(41L, 7L)).thenReturn(detail);

        OrderDetailDTO result = orderStatusService.reject(41L, 7L);

        verify(taskStatusService).reopen(11L);
        verify(applicationEventPublisher).publishEvent(any(OrderRejectedEvent.class));
        assertEquals(detail, result);
    }

    @Test
    void abandonConfirmedNormalTaskPenalizesHelperAndReopensTask() {
        Order order = confirmedOrder(51L, 12L, 7L, 9L);
        Task task = normalTask(12L, 7L);
        when(orderService.requireAccessibleOrder(51L, 9L)).thenReturn(order);
        when(taskService.requireTask(12L)).thenReturn(task);
        when(orderMapper.updateStatusAndVersion(51L, 3, 1, 0)).thenReturn(1);
        when(orderService.getDetail(51L, 9L)).thenReturn(detail(51L, "CANCELLED"));

        orderStatusService.abandon(51L, 9L);

        verify(taskStatusService).reopen(12L);
        verify(creditService).adjustCreditScore(eq(9L), eq(-5), anyString(), eq(51L), isNull());
    }

    @Test
    void confirmRejectsNonPoster() {
        Order order = pendingOrder(61L, 13L, 7L, 9L);
        when(orderService.requireAccessibleOrder(61L, 9L)).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> orderStatusService.confirm(61L, 9L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    @Test
    void confirmTeamUpCompletesFullTeamAndCancelsPendingRequests() {
        Order order = pendingOrder(71L, 14L, 7L, 9L);
        Task teamTask = teamUpTask(14L, 7L, 4, 3, TaskCodecs.TASK_STATUS_IN_PROGRESS);
        Task completedTask = teamUpTask(14L, 7L, 4, 4, TaskCodecs.TASK_STATUS_COMPLETED);
        when(orderService.requireAccessibleOrder(71L, 7L)).thenReturn(order);
        when(taskService.requireTask(14L)).thenReturn(teamTask, completedTask);
        when(orderMapper.updateStatusAndVersion(71L, 1, 0, 0)).thenReturn(1);
        when(taskStatusService.incrementTeamCurrentMembers(14L)).thenReturn(true);
        when(orderService.getDetail(71L, 7L)).thenReturn(detail(71L, "CONFIRMED"));

        orderStatusService.confirm(71L, 7L);

        verify(orderMapper).completeConfirmedOrdersByTask(14L);
        verify(orderMapper).cancelPendingOrdersByTask(14L);
    }

    @Test
    void confirmTeamUpRejectsWhenRosterIsAlreadyFull() {
        Order order = pendingOrder(72L, 14L, 7L, 9L);
        when(orderService.requireAccessibleOrder(72L, 7L)).thenReturn(order);
        when(taskService.requireTask(14L)).thenReturn(teamUpTask(14L, 7L, 4, 3, TaskCodecs.TASK_STATUS_IN_PROGRESS));
        when(orderMapper.updateStatusAndVersion(72L, 1, 0, 0)).thenReturn(1);
        when(taskStatusService.incrementTeamCurrentMembers(14L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> orderStatusService.confirm(72L, 7L));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void completeRewardsBothUsersAndMarksTaskCompleted() {
        Order order = confirmedOrder(81L, 15L, 7L, 9L);
        when(orderService.requireAccessibleOrder(81L, 7L)).thenReturn(order);
        when(taskService.requireTask(15L)).thenReturn(normalTask(15L, 7L));
        when(orderMapper.updateStatusAndVersion(81L, 2, 1, 0)).thenReturn(1);
        when(orderService.getDetail(81L, 7L)).thenReturn(detail(81L, "COMPLETED"));

        orderStatusService.complete(81L, 7L);

        verify(taskStatusService).markCompleted(15L);
        verify(creditService).adjustCreditScore(eq(7L), eq(1), anyString(), eq(81L), isNull());
        verify(creditService).adjustCreditScore(eq(9L), eq(2), anyString(), eq(81L), isNull());
    }

    @Test
    void completeRejectsTeamUpTask() {
        Order order = confirmedOrder(82L, 15L, 7L, 9L);
        when(orderService.requireAccessibleOrder(82L, 7L)).thenReturn(order);
        when(taskService.requireTask(15L)).thenReturn(teamUpTask(15L, 7L, 4, 4, TaskCodecs.TASK_STATUS_COMPLETED));

        BusinessException exception = assertThrows(BusinessException.class, () -> orderStatusService.complete(82L, 7L));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void cancelMarksTaskCancelledAndPenalizesPosterAfterConfirmation() {
        Order order = confirmedOrder(91L, 16L, 7L, 9L);
        when(orderService.requireAccessibleOrder(91L, 7L)).thenReturn(order);
        when(taskService.requireTask(16L)).thenReturn(normalTask(16L, 7L));
        when(orderMapper.selectActiveOrdersByTask(16L)).thenReturn(List.of(order));
        when(orderService.getDetail(91L, 7L)).thenReturn(detail(91L, "CANCELLED"));

        orderStatusService.cancel(91L, 7L);

        verify(orderMapper).cancelOrdersByTask(16L);
        verify(taskStatusService).markCancelled(16L);
        verify(creditService).adjustCreditScore(eq(7L), eq(-3), anyString(), eq(91L), isNull());
    }

    @Test
    void cancelPendingOrderDoesNotPenalizePoster() {
        Order order = pendingOrder(92L, 17L, 7L, 9L);
        when(orderService.requireAccessibleOrder(92L, 7L)).thenReturn(order);
        when(taskService.requireTask(17L)).thenReturn(normalTask(17L, 7L));
        when(orderMapper.selectActiveOrdersByTask(17L)).thenReturn(List.of(order));
        when(orderService.getDetail(92L, 7L)).thenReturn(detail(92L, "CANCELLED"));

        orderStatusService.cancel(92L, 7L);

        verify(taskStatusService).markCancelled(17L);
        verify(creditService, never()).adjustCreditScore(eq(7L), eq(-3), anyString(), eq(92L), isNull());
    }

    private static Order pendingOrder(Long orderId, Long taskId, Long posterId, Long helperId) {
        Order order = new Order();
        order.setId(orderId);
        order.setTaskId(taskId);
        order.setPosterId(posterId);
        order.setHelperId(helperId);
        order.setStatus(0);
        order.setVersion(0);
        return order;
    }

    private static Order confirmedOrder(Long orderId, Long taskId, Long posterId, Long helperId) {
        Order order = pendingOrder(orderId, taskId, posterId, helperId);
        order.setStatus(1);
        return order;
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

    private static Task teamUpTask(Long taskId, Long publisherId, int total, int current, int status) {
        Task task = new Task();
        task.setId(taskId);
        task.setPublisherId(publisherId);
        task.setTitle("Team up");
        task.setCategory(TaskCodecs.TASK_CATEGORY_TEAM_UP);
        task.setTeamTotalMembers(total);
        task.setTeamCurrentMembers(current);
        task.setStatus(status);
        return task;
    }

    private static OrderDetailDTO detail(Long orderId, String status) {
        return new OrderDetailDTO(
                orderId,
                11L,
                "Need pickup",
                "desc",
                "EXPRESS",
                "OPEN",
                "Dorm",
                BigDecimal.TEN,
                status,
                false,
                null,
                null,
                7L,
                "Poster",
                null,
                90,
                9L,
                "Helper",
                null,
                90,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
