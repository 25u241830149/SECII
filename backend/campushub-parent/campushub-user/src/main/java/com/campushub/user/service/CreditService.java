package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.CreditDTO;
import com.campushub.user.entity.User;
import com.campushub.user.mapper.CreditMapper;
import com.campushub.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditService {

    private final UserMapper userMapper;
    private final CreditMapper creditMapper;

    public CreditService(UserMapper userMapper, CreditMapper creditMapper) {
        this.userMapper = userMapper;
        this.creditMapper = creditMapper;
    }

    public CreditDTO getCredit(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户 ID 不能为空");
        }
        User user = userMapper.selectActiveById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        int creditScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
        long completedOrderCount = creditMapper.countCompletedOrdersByHelper(userId);
        long cancelledOrderCount = creditMapper.countCancelledOrdersByHelper(userId);
        long finishedOrderCount = completedOrderCount + cancelledOrderCount;
        return new CreditDTO(
                creditScore,
                creditLevel(creditScore),
                ratio(completedOrderCount, finishedOrderCount),
                ratio(cancelledOrderCount, finishedOrderCount),
                creditMapper.averageReceivedRating(userId),
                creditMapper.countReceivedReviews(userId),
                creditMapper.countPublishedTasks(userId),
                completedOrderCount,
                cancelledOrderCount,
                creditMapper.selectRecentRecords(userId, 10)
        );
    }

    @Transactional
    public void adjustCreditScore(Long userId, int delta, String reason, Long orderId, Long reviewId) {
        if (userMapper.adjustCreditScore(userId, delta) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        Integer scoreAfter = userMapper.selectCreditScore(userId);
        if (scoreAfter == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        creditMapper.insertCreditRecord(userId, reason, delta, scoreAfter, orderId, reviewId);
    }

    private static double ratio(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / denominator;
    }

    static String creditLevel(int creditScore) {
        if (creditScore >= 90) {
            return "诚信学生";
        }
        if (creditScore >= 80) {
            return "可靠同学";
        }
        if (creditScore >= 60) {
            return "普通用户";
        }
        return "信用预警";
    }
}
