package com.campushub.review.mapper;

import com.campushub.review.dto.ReviewDTO;
import com.campushub.review.entity.Review;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReviewMapper {

    int insertReview(Review review);

    ReviewDTO selectReviewById(@Param("reviewId") Long reviewId);

    Review selectReviewByOrderAndReviewer(
            @Param("orderId") Long orderId,
            @Param("reviewerId") Long reviewerId
    );

    List<ReviewDTO> selectReviewsByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countReviewsByUser(@Param("userId") Long userId);

    List<ReviewDTO> selectReviewsByOrder(@Param("orderId") Long orderId);

    BigDecimal averageRatingForTarget(@Param("targetUserId") Long targetUserId);

    long countReviewsForTarget(@Param("targetUserId") Long targetUserId);

    long countAllReviews();

    BigDecimal averageAllRating();
}
