package com.campushub.review.service;

import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDetailDTO;
import com.campushub.review.dto.ReviewListDTO;
import java.util.List;

public interface ReviewService {

    void createReview(ReviewCreateRequest request);

    ReviewDetailDTO getReviewDetail(Long reviewId);

    List<ReviewListDTO> listUserReviews(Long userId);
}