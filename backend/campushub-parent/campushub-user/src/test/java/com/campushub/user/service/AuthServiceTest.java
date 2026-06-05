package com.campushub.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserVerificationMapper verificationMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserAndPendingVerification() {
        RegisterRequest request = new RegisterRequest(
                "20260001",
                "CampusHub123",
                "Alice",
                "张三",
                "软件学院",
                "student-cards/20260001.jpg"
        );
        when(userMapper.existsByStudentId("20260001")).thenReturn(0);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return 1;
        });

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserVerification> verificationCaptor = ArgumentCaptor.forClass(UserVerification.class);
        verify(userMapper).insert(userCaptor.capture());
        verify(verificationMapper).insert(verificationCaptor.capture());
        User user = userCaptor.getValue();
        UserVerification verification = verificationCaptor.getValue();
        assertEquals("20260001", user.getStudentId());
        assertEquals("Alice", user.getNickname());
        assertEquals("张三", user.getRealName());
        assertEquals("软件学院", user.getDepartment());
        assertEquals(0, user.getRole());
        assertEquals(90, user.getCreditScore());
        assertEquals(1, user.getStatus());
        assertNotEquals("CampusHub123", user.getPassword());
        assertTrue(EncryptUtils.matchesPassword("CampusHub123", user.getPassword()));
        assertEquals(11L, verification.getUserId());
        assertEquals("张三", verification.getRealName());
        assertEquals("student-cards/20260001.jpg", verification.getStudentCardImage());
        assertEquals(0, verification.getStatus());
    }

    @Test
    void registerRejectsDuplicateStudentId() {
        RegisterRequest request = new RegisterRequest(
                "20260001",
                "CampusHub123",
                "Alice",
                "张三",
                "软件学院",
                "student-cards/20260001.jpg"
        );
        when(userMapper.existsByStudentId("20260001")).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void registerRejectsIncompleteRequestBeforeDatabaseAccess() {
        RegisterRequest request = new RegisterRequest(
                " ",
                "CampusHub123",
                "Alice",
                "Alice Real",
                "软件学院",
                "student-cards/20260001.jpg"
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
        verifyNoInteractions(userMapper, verificationMapper, jwtTokenProvider);
    }

    @Test
    void loginRejectsIncompleteRequestBeforeDatabaseAccess() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("20260001", " "))
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
        verifyNoInteractions(userMapper, verificationMapper, jwtTokenProvider);
    }

    @Test
    void loginReturnsJwtAndUserInfo() {
        User user = new User();
        user.setId(7L);
        user.setStudentId("20260001");
        user.setPassword(EncryptUtils.encryptPassword("CampusHub123"));
        user.setNickname("Alice");
        user.setAvatarUrl("avatar.png");
        user.setRole(0);
        user.setCreditScore(98);
        user.setStatus(0);
        UserVerification verification = new UserVerification();
        verification.setStatus(1);
        when(userMapper.selectActiveByStudentId("20260001")).thenReturn(user);
        when(verificationMapper.selectLatestByUserId(7L)).thenReturn(verification);
        when(jwtTokenProvider.generateAccessToken(7L, "20260001", UserRole.USER)).thenReturn("jwt-token");

        LoginResponseDTO response = authService.login(new LoginRequest("20260001", "CampusHub123"));

        assertEquals("jwt-token", response.token());
        assertEquals(7L, response.user().userId());
        assertEquals("20260001", response.user().studentId());
        assertEquals("Alice", response.user().nickname());
        assertEquals("APPROVED", response.user().verificationStatus());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = new User();
        user.setPassword(EncryptUtils.encryptPassword("CampusHub123"));
        when(userMapper.selectActiveByStudentId("20260001")).thenReturn(user);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("20260001", "wrong-password"))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getCode());
    }

    @Test
    void loginRejectsUnknownStudentId() {
        when(userMapper.selectActiveByStudentId("20269999")).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("20269999", "CampusHub123"))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getCode());
    }

    @Test
    void loginRejectsBannedUser() {
        User user = new User();
        user.setPassword(EncryptUtils.encryptPassword("CampusHub123"));
        user.setStatus(2);
        when(userMapper.selectActiveByStudentId("20260001")).thenReturn(user);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("20260001", "CampusHub123"))
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    @Test
    void loginUsesAdminRoleWhenStoredRoleIsOne() {
        User user = new User();
        user.setId(1L);
        user.setStudentId("admin");
        user.setPassword(EncryptUtils.encryptPassword("CampusHub123"));
        user.setRole(1);
        user.setStatus(0);
        when(userMapper.selectActiveByStudentId("admin")).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(1L, "admin", UserRole.ADMIN)).thenReturn("admin-token");

        authService.login(new LoginRequest("admin", "CampusHub123"));

        verify(jwtTokenProvider).generateAccessToken(eq(1L), eq("admin"), eq(UserRole.ADMIN));
    }
}
