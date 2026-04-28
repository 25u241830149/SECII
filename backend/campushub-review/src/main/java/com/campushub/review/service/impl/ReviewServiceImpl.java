package com.campushub.review.service.impl;

import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDetailDTO;
import com.campushub.review.dto.ReviewListDTO;
import com.campushub.review.service.ReviewService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Override
    public void createReview(ReviewCreateRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public ReviewDetailDTO getReviewDetail(Long reviewId) {
        return ReviewDetailDTO.builder()
                .id(reviewId)
                .score(0)
                .build();
    }

    @Override
    public List<ReviewListDTO> listUserReviews(Long userId) {
        return Collections.emptyList();
    }
}