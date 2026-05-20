package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.AdminVerifyRequest;
import com.campushub.user.dto.AdminVerifyResultDTO;
import com.campushub.user.dto.VerificationStatusDTO;
import com.campushub.user.dto.VerificationSubmitRequest;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import com.campushub.user.mapper.UserMapper;
import com.campushub.user.mapper.UserVerificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationService {

    private static final int USER_STATUS_PENDING_VERIFICATION = 1;
    private static final int USER_STATUS_NORMAL = 0;
    private static final int VERIFICATION_PENDING = 0;
    private static final int VERIFICATION_APPROVED = 1;
    private static final int VERIFICATION_REJECTED = 2;

    private final UserMapper userMapper;
    private final UserVerificationMapper verificationMapper;

    public VerificationService(UserMapper userMapper, UserVerificationMapper verificationMapper) {
        this.userMapper = userMapper;
        this.verificationMapper = verificationMapper;
    }

    @Transactional
    public VerificationStatusDTO submit(VerificationSubmitRequest request) {
        validateSubmitRequest(request);
        User user = userMapper.selectActiveById(request.userId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (verificationMapper.selectPendingByUserId(request.userId()) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "已有待审核实名认证");
        }

        UserVerification verification = new UserVerification();
        verification.setUserId(request.userId());
        verification.setRealName(request.realName());
        verification.setStudentCardImage(request.studentCardImage());
        verification.setStatus(VERIFICATION_PENDING);
        verificationMapper.insert(verification);

        User update = new User();
        update.setId(request.userId());
        update.setStatus(USER_STATUS_PENDING_VERIFICATION);
        userMapper.updateById(update);

        // TODO(message): notify admins through NotificationGateway after the
        // message module is available.
        return new VerificationStatusDTO("PENDING");
    }

    @Transactional
    public AdminVerifyResultDTO review(Long userId, Long reviewerId, AdminVerifyRequest request) {
        if (userId == null || reviewerId == null || request == null || request.approved() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "审核信息不完整");
        }
        UserVerification latest = verificationMapper.selectLatestByUserId(userId);
        if (latest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "实名认证记录不存在");
        }

        boolean approved = request.approved();
        UserVerification updateVerification = new UserVerification();
        updateVerification.setId(latest.getId());
        updateVerification.setReviewerId(reviewerId);
        updateVerification.setStatus(approved ? VERIFICATION_APPROVED : VERIFICATION_REJECTED);
        updateVerification.setRejectReason(approved ? null : request.remark());
        verificationMapper.updateById(updateVerification);

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(approved ? USER_STATUS_NORMAL : USER_STATUS_PENDING_VERIFICATION);
        userMapper.updateById(updateUser);

        String status = approved ? "APPROVED" : "REJECTED";
        // TODO(message): notify the user of approval/rejection once real-time
        // or persistent notification infrastructure is connected.
        return new AdminVerifyResultDTO(userId, status, request.remark());
    }

    private static void validateSubmitRequest(VerificationSubmitRequest request) {
        if (request == null
                || request.userId() == null
                || isBlank(request.realName())
                || isBlank(request.studentId())
                || isBlank(request.college())
                || isBlank(request.studentCardImage())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "认证信息不完整");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
