package com.campushub.review.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.common.utils.ValidateUtils;
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
import com.campushub.user.entity.User;
import com.campushub.user.service.CreditService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private static final int ORDER_STATUS_COMPLETED = 2;
    private static final int MAX_CONTENT_LENGTH = 500;

    private final ReviewMapper reviewMapper;
    private final OrderService orderService;
    private final TaskService taskService;
    private final CreditService creditService;
    private final CreditCalculator creditCalculator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ReviewService(
            ReviewMapper reviewMapper,
            OrderService orderService,
            TaskService taskService,
            CreditService creditService,
            CreditCalculator creditCalculator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.reviewMapper = reviewMapper;
        this.orderService = orderService;
        this.taskService = taskService;
        this.creditService = creditService;
        this.creditCalculator = creditCalculator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public ReviewDTO create(Long reviewerId, ReviewCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价内容不能为空");
        }
        ValidateUtils.requirePositive(request.orderId(), "orderId");
        ValidateUtils.requirePositive(request.targetUserId(), "targetUserId");
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评分必须在 1 到 5 之间");
        }

        Order order = orderService.requireAccessibleOrder(request.orderId(), reviewerId);
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_COMPLETED)) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单完成后才能评价");
        }
        Task task = taskService.requireTask(order.getTaskId());
        if (Objects.equals(task.getCategory(), TaskCodecs.TASK_CATEGORY_TEAM_UP)) {
            throw new BusinessException(ErrorCode.CONFLICT, "活动组队无需评价");
        }
        Long expectedTarget = expectedTarget(order, reviewerId);
        if (!Objects.equals(expectedTarget, request.targetUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价对象必须是订单另一方");
        }
        if (reviewMapper.selectReviewByOrderAndReviewer(request.orderId(), reviewerId) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "你已评价过该订单");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setReviewerId(reviewerId);
        review.setTargetUserId(request.targetUserId());
        review.setRating(request.rating());
        review.setContent(normalizeContent(request.content()));
        try {
            reviewMapper.insertReview(review);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "你已评价过该订单", ex);
        }

        creditService.adjustCreditScore(
                request.targetUserId(),
                creditCalculator.adjustmentForRating(request.rating()),
                "收到" + request.rating() + "星评价",
                review.getOrderId(),
                review.getId()
        );
        applicationEventPublisher.publishEvent(new ReviewCreatedEvent(
                review.getId(),
                review.getOrderId(),
                review.getReviewerId(),
                review.getTargetUserId(),
                review.getRating()
        ));
        return getDetail(review.getId());
    }

    public ReviewDTO getDetail(Long reviewId) {
        ValidateUtils.requirePositive(reviewId, "reviewId");
        ReviewDTO review = reviewMapper.selectReviewById(reviewId);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在");
        }
        return review;
    }

    public PageResponse<ReviewDTO> listByUser(Long userId, Integer page, Integer size) {
        ValidateUtils.requirePositive(userId, "userId");
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        List<ReviewDTO> records = reviewMapper.selectReviewsByUser(userId, offset, normalizedSize);
        long total = reviewMapper.countReviewsByUser(userId);
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }

    public List<ReviewDTO> listByOrder(Long orderId, Long currentUserId) {
        orderService.requireAccessibleOrder(orderId, currentUserId);
        return reviewMapper.selectReviewsByOrder(orderId);
    }

    public long countAllReviews() {
        return reviewMapper.countAllReviews();
    }

    public BigDecimal averageAllRating() {
        return reviewMapper.averageAllRating();
    }

    private Long expectedTarget(Order order, Long reviewerId) {
        if (Objects.equals(order.getPosterId(), reviewerId)) {
            return order.getHelperId();
        }
        if (Objects.equals(order.getHelperId(), reviewerId)) {
            return order.getPosterId();
        }
        if (SecurityUtils.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员不能代替用户评价订单");
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN_MESSAGE);
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String normalized = content.trim();
        if (normalized.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价内容不能超过 500 个字符");
        }
        return normalized;
    }

}
