package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.AdminBanRequest;
import com.campushub.user.dto.AdminBanResultDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserProfileUpdateRequest;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import com.campushub.user.mapper.UserMapper;
import com.campushub.user.mapper.UserVerificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int USER_STATUS_BANNED = 2;

    private final UserMapper userMapper;
    private final UserVerificationMapper verificationMapper;

    public UserService(UserMapper userMapper, UserVerificationMapper verificationMapper) {
        this.userMapper = userMapper;
        this.verificationMapper = verificationMapper;
    }

    public UserProfileDTO getProfile(Long userId) {
        User user = requireActiveUser(userId);
        UserVerification verification = verificationMapper.selectLatestByUserId(userId);
        return UserDtoAssembler.toUserProfile(user, verification);
    }

    public UserPublicDTO getPublicUser(Long userId) {
        return UserDtoAssembler.toUserPublic(requireActiveUser(userId));
    }

    public UserHomeDTO getHome(Long userId) {
        User user = requireActiveUser(userId);
        int creditScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
        // TODO(order/review/task): replace Sprint 1 placeholders with aggregate counts
        // from published tasks, completed orders, and received reviews.
        return new UserHomeDTO(
                user.getId(),
                user.getNickname(),
                user.getAvatarUrl(),
                creditScore,
                CreditService.creditLevel(creditScore),
                0,
                0,
                0
        );
    }

    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request) {
        requireActiveUser(userId);
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "资料信息不能为空");
        }

        User update = new User();
        update.setId(userId);
        update.setNickname(request.nickname());
        update.setAvatarUrl(request.avatarUrl());
        userMapper.updateById(update);
        return getProfile(userId);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        requireActiveUser(userId);
        // TODO(order): reject deletion when the user still has unfinished orders,
        // matching the API contract's 422 account-cancellation rule.
        User update = new User();
        update.setId(userId);
        update.setIsDeleted(true);
        userMapper.updateById(update);
    }

    @Transactional
    public AdminBanResultDTO banUser(Long userId, AdminBanRequest request) {
        requireActiveUser(userId);
        // TODO(admin/schema): persist ban duration or moderation records once the
        // schema adds ban expiry/history columns; current u_user only stores status.
        User update = new User();
        update.setId(userId);
        update.setStatus(USER_STATUS_BANNED);
        userMapper.updateById(update);

        String reason = request == null || request.reason() == null || request.reason().isBlank()
                ? "违规封禁"
                : request.reason();
        return new AdminBanResultDTO(userId, "BANNED", reason);
    }

    private User requireActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户 ID 不能为空");
        }
        User user = userMapper.selectActiveById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }
}
