package com.campushub.review.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDTO;
import com.campushub.review.service.ReviewService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<ReviewDTO> create(@RequestBody ReviewCreateRequest request) {
        Long reviewerId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(reviewService.create(reviewerId, request));
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDTO> detail(@PathVariable Long reviewId) {
        return ApiResponse.success(reviewService.getDetail(reviewId));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<ReviewDTO>> byUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ApiResponse.success(reviewService.listByUser(userId, page, size));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<ReviewDTO>> byOrder(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(reviewService.listByOrder(orderId, currentUserId));
    }
}
