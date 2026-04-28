package com.campushub.review.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDetailDTO;
import com.campushub.review.dto.ReviewListDTO;
import com.campushub.review.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<String> createReview(@RequestBody ReviewCreateRequest request) {
        reviewService.createReview(request);
        return ApiResponse.success("Review module scaffold ready", null);
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDetailDTO> getReviewDetail(@PathVariable Long reviewId) {
        return ApiResponse.success(reviewService.getReviewDetail(reviewId));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<ReviewListDTO>> getUserReviews(@PathVariable Long userId,
                                                                   @RequestParam(defaultValue = "1") Long page,
                                                                   @RequestParam(defaultValue = "10") Long size) {
        List<ReviewListDTO> reviews = reviewService.listUserReviews(userId);
        return ApiResponse.success(new PageResponse<>(reviews, (long) reviews.size(), page, size));
    }
}