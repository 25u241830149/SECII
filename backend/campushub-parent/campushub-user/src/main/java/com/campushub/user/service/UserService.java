package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.utils.EncryptUtils;
import com.campushub.user.dto.AdminBanRequest;
import com.campushub.user.dto.AdminBanResultDTO;
import com.campushub.user.dto.ChangePasswordRequest;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserProfileUpdateRequest;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import com.campushub.user.mapper.CreditMapper;
import com.campushub.user.mapper.UserMapper;
import com.campushub.user.mapper.UserVerificationMapper;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int USER_STATUS_BANNED = 2;
    private static final int MAX_NICKNAME_LENGTH = 64;
    private static final int MAX_EMAIL_LENGTH = 128;
    private static final int MAX_PHONE_LENGTH = 32;
    private static final int MAX_AVATAR_URL_LENGTH = 255;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    private final UserMapper userMapper;
    private final UserVerificationMapper verificationMapper;
    private final CreditMapper creditMapper;

    public UserService(UserMapper userMapper, UserVerificationMapper verificationMapper, CreditMapper creditMapper) {
        this.userMapper = userMapper;
        this.verificationMapper = verificationMapper;
        this.creditMapper = creditMapper;
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
        return new UserHomeDTO(
                user.getId(),
                user.getNickname(),
                user.getAvatarUrl(),
                creditScore,
                CreditService.creditLevel(creditScore),
                Math.toIntExact(creditMapper.countPublishedTasks(userId)),
                Math.toIntExact(creditMapper.countCompletedOrdersByUser(userId)),
                Math.toIntExact(creditMapper.countReceivedReviews(userId)),
                creditMapper.averageReceivedRating(userId)
        );
    }

    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request) {
        requireActiveUser(userId);
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "资料信息不能为空");
        }

        String nickname = requiredTrim(request.nickname(), "昵称不能为空");
        String email = optionalTrim(request.email());
        String phone = optionalTrim(request.phone());
        String avatarUrl = optionalTrim(request.avatarUrl());

        validateMaxLength(nickname, MAX_NICKNAME_LENGTH, "昵称不能超过 64 个字符");
        validateMaxLength(email, MAX_EMAIL_LENGTH, "邮箱不能超过 128 个字符");
        validateMaxLength(phone, MAX_PHONE_LENGTH, "手机号不能超过 32 个字符");
        validateMaxLength(avatarUrl, MAX_AVATAR_URL_LENGTH, "头像地址不能超过 255 个字符");

        if (email != null && !email.matches(EMAIL_PATTERN)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱格式不正确");
        }
        if (phone != null && !phone.matches(PHONE_PATTERN)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        validateAvatarUrl(avatarUrl);

        User update = new User();
        update.setId(userId);
        update.setEmail(email);
        update.setPhone(phone);
        update.setNickname(nickname);
        update.setAvatarUrl(avatarUrl);
        userMapper.updateById(update);
        return getProfile(userId);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = requireActiveUser(userId);
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码信息不能为空");
        }

        String currentPassword = request.currentPassword();
        String newPassword = request.newPassword();
        String confirmPassword = request.confirmPassword();

        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请完整填写密码信息");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "两次输入的新密码不一致");
        }
        if (!EncryptUtils.matchesPassword(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前密码错误");
        }
        if (EncryptUtils.matchesPassword(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能与当前密码相同");
        }

        User update = new User();
        update.setId(userId);
        update.setPassword(EncryptUtils.encryptPassword(newPassword));
        userMapper.updateById(update);
    }

    private static String requiredTrim(String value, String message) {
        String trimmed = optionalTrim(value);
        if (trimmed == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return trimmed;
    }

    private static String optionalTrim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static void validateMaxLength(String value, int maxLength, String message) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private static void validateAvatarUrl(String avatarUrl) {
        if (avatarUrl == null) {
            return;
        }
        if (avatarUrl.startsWith("/uploads/avatars/")) {
            return;
        }
        try {
            URI uri = new URI(avatarUrl);
            String scheme = uri.getScheme();
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null) {
                return;
            }
        } catch (URISyntaxException ignored) {
            // fall through to business exception
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "头像地址格式不正确");
    }

    @Transactional
    public void deleteAccount(Long userId) {
        requireActiveUser(userId);
        // TODO(order): reject deletion when the user still has unfinished orders,
        // matching the API contract's 422 account-cancellation rule.
        int deleted = userMapper.deleteById(userId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
