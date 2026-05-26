package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.security.JwtTokenProvider;
import com.campushub.common.utils.EncryptUtils;
import com.campushub.user.dto.LoginRequest;
import com.campushub.user.dto.LoginResponseDTO;
import com.campushub.user.dto.RegisterRequest;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import com.campushub.user.mapper.UserMapper;
import com.campushub.user.mapper.UserVerificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int ROLE_USER = 0;
    private static final int DEFAULT_CREDIT_SCORE = 100;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int USER_STATUS_PENDING_VERIFICATION = 1;
    private static final int USER_STATUS_BANNED = 2;
    private static final int VERIFICATION_PENDING = 0;

    private final UserMapper userMapper;
    private final UserVerificationMapper verificationMapper;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserMapper userMapper,
            UserVerificationMapper verificationMapper,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userMapper = userMapper;
        this.verificationMapper = verificationMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        validateRegisterRequest(request);
        if (userMapper.existsByStudentId(request.studentId()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "学号已注册");
        }

        User user = new User();
        user.setStudentId(request.studentId());
        user.setPassword(EncryptUtils.encryptPassword(request.password()));
        user.setNickname(request.nickname());
        user.setRealName(request.realName());
        user.setDepartment(request.department());
        user.setRole(ROLE_USER);
        user.setCreditScore(DEFAULT_CREDIT_SCORE);
        user.setStatus(USER_STATUS_PENDING_VERIFICATION);
        user.setIsDeleted(false);
        userMapper.insert(user);

        UserVerification verification = new UserVerification();
        verification.setUserId(user.getId());
        verification.setRealName(request.realName());
        verification.setStudentCardImage(request.studentCardImage());
        verification.setStatus(VERIFICATION_PENDING);
        verificationMapper.insert(verification);
    }

    public LoginResponseDTO login(LoginRequest request) {
        validateLoginRequest(request);
        User user = userMapper.selectActiveByStudentId(request.studentId());
        if (user == null || !EncryptUtils.matchesPassword(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "学号或密码错误");
        }
        if (Integer.valueOf(USER_STATUS_BANNED).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被封禁");
        }

        UserRole role = UserDtoAssembler.roleFromCode(user.getRole());
        String token = jwtTokenProvider.generateAccessToken(user.getId(), user.getStudentId(), role);
        UserVerification verification = verificationMapper.selectLatestByUserId(user.getId());
        return new LoginResponseDTO(token, UserDtoAssembler.toUserInfo(user, verification));
    }

    private static void validateRegisterRequest(RegisterRequest request) {
        if (request == null
                || isBlank(request.studentId())
                || isBlank(request.password())
                || isBlank(request.nickname())
                || isBlank(request.realName())
                || isBlank(request.department())
                || isBlank(request.studentCardImage())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "注册信息不完整");
        }
        if (request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度不能少于 6 位");
        }
    }

    private static void validateLoginRequest(LoginRequest request) {
        if (request == null || isBlank(request.studentId()) || isBlank(request.password())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录信息不完整");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
