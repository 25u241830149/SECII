package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.CreditDTO;
import com.campushub.user.entity.User;
import com.campushub.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class CreditService {

    private final UserMapper userMapper;

    public CreditService(UserMapper userMapper) {
        this.userMapper = userMapper;
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
        // TODO(order/review): calculate completedRate/cancelledRate and refresh
        // creditScore from order fulfillment, cancellations, and review outcomes.
        return new CreditDTO(
                creditScore,
                creditLevel(creditScore),
                0.0,
                0.0
        );
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
