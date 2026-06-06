package com.campushub.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.order.entity.Order;
import com.campushub.order.service.OrderService;
import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDTO;
import com.campushub.review.entity.Review;
import com.campushub.review.event.ReviewCreatedEvent;
import com.campushub.review.mapper.ReviewMapper;
import com.campushub.task.entity.Task;
import com.campushub.task.service.TaskCodecs;
import com.campushub.task.service.TaskService;
import com.campushub.user.service.CreditService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private TaskService taskService;

    @Mock
    private CreditService creditService;

    @Mock
    private CreditCalculator creditCalculator;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createRejectsReviewForTeamUpTask() {
        when(orderService.requireAccessibleOrder(11L, 7L)).thenReturn(completedOrder(11L, 31L, 7L, 9L));
        Task task = new Task();
        task.setId(31L);
        task.setCategory(TaskCodecs.TASK_CATEGORY_TEAM_UP);
        when(taskService.requireTask(31L)).thenReturn(task);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.create(7L, new ReviewCreateRequest(11L, 9L, 5, "Great"))
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void createRejectsDuplicateReview() {
        when(orderService.requireAccessibleOrder(12L, 7L)).thenReturn(completedOrder(12L, 32L, 7L, 9L));
        Task task = new Task();
        task.setId(32L);
        task.setCategory(TaskCodecs.TASK_CATEGORY_EXPRESS);
        when(taskService.requireTask(32L)).thenReturn(task);
        when(reviewMapper.selectReviewByOrderAndReviewer(12L, 7L)).thenReturn(new Review());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.create(7L, new ReviewCreateRequest(12L, 9L, 5, "Great"))
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void createAdjustsCreditAndPublishesEvent() {
        when(orderService.requireAccessibleOrder(13L, 7L)).thenReturn(completedOrder(13L, 33L, 7L, 9L));
        Task task = new Task();
        task.setId(33L);
        task.setCategory(TaskCodecs.TASK_CATEGORY_EXPRESS);
        when(taskService.requireTask(33L)).thenReturn(task);
        when(reviewMapper.insertReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(99L);
            return 1;
        });
        when(creditCalculator.adjustmentForRating(5)).thenReturn(3);
        ReviewDTO detail = new ReviewDTO(
                99L,
                13L,
                7L,
                "Poster",
                null,
                9L,
                "Helper",
                null,
                5,
                "Great",
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
        when(reviewMapper.selectReviewById(99L)).thenReturn(detail);

        ReviewDTO result = reviewService.create(7L, new ReviewCreateRequest(13L, 9L, 5, " Great "));

        verify(creditService).adjustCreditScore(9L, 3, "收到5星评价", 13L, 99L);
        verify(applicationEventPublisher).publishEvent(any(ReviewCreatedEvent.class));
        assertEquals(detail, result);
    }

    @Test
    void createRejectsWrongTargetUser() {
        when(orderService.requireAccessibleOrder(14L, 7L)).thenReturn(completedOrder(14L, 34L, 7L, 9L));
        Task task = new Task();
        task.setId(34L);
        task.setCategory(TaskCodecs.TASK_CATEGORY_EXPRESS);
        when(taskService.requireTask(34L)).thenReturn(task);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.create(7L, new ReviewCreateRequest(14L, 8L, 5, "Great"))
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    private static Order completedOrder(Long orderId, Long taskId, Long posterId, Long helperId) {
        Order order = new Order();
        order.setId(orderId);
        order.setTaskId(taskId);
        order.setPosterId(posterId);
        order.setHelperId(helperId);
        order.setStatus(2);
        return order;
    }
}
