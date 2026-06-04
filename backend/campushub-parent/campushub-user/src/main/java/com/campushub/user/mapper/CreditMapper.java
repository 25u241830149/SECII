package com.campushub.user.mapper;

import com.campushub.user.dto.CreditRecordDTO;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CreditMapper {

    @Select("""
            SELECT COUNT(1)
            FROM t_task
            WHERE publisher_id = #{userId}
              AND is_deleted = false
            """)
    long countPublishedTasks(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM t_order
            WHERE helper_id = #{userId}
              AND status = 2
              AND is_deleted = false
            """)
    long countCompletedOrdersByHelper(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM t_order
            WHERE helper_id = #{userId}
              AND status = 3
              AND is_deleted = false
            """)
    long countCancelledOrdersByHelper(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM t_order
            WHERE (poster_id = #{userId} OR helper_id = #{userId})
              AND status = 2
              AND is_deleted = false
            """)
    long countCompletedOrdersByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM t_review
            WHERE target_user_id = #{userId}
            """)
    long countReceivedReviews(@Param("userId") Long userId);

    @Select("""
            SELECT AVG(rating)
            FROM t_review
            WHERE target_user_id = #{userId}
            """)
    BigDecimal averageReceivedRating(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO t_credit_record (
                user_id, reason, delta, score_after, order_id, review_id
            ) VALUES (
                #{userId}, #{reason}, #{delta}, #{scoreAfter}, #{orderId}, #{reviewId}
            )
            """)
    int insertCreditRecord(
            @Param("userId") Long userId,
            @Param("reason") String reason,
            @Param("delta") int delta,
            @Param("scoreAfter") int scoreAfter,
            @Param("orderId") Long orderId,
            @Param("reviewId") Long reviewId
    );

    @Select("""
            SELECT
                id AS recordId,
                reason,
                delta,
                score_after AS scoreAfter,
                create_time AS createdAt
            FROM t_credit_record
            WHERE user_id = #{userId}
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            """)
    List<CreditRecordDTO> selectRecentRecords(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );
}
