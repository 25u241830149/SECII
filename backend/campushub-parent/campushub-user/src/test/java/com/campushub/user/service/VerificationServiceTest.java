package com.campushub.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserVerificationMapper verificationMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    void submitCreatesPendingVerificationAndMarksUserPending() {
        User user = new User();
        user.setId(7L);
        when(userMapper.selectActiveById(7L)).thenReturn(user);
        when(verificationMapper.selectPendingByUserId(7L)).thenReturn(null);

        VerificationStatusDTO result = verificationService.submit(
                new VerificationSubmitRequest(7L, "张三", "20260001", "软件学院", "student-cards/7.jpg")
        );

        ArgumentCaptor<UserVerification> verificationCaptor = ArgumentCaptor.forClass(UserVerification.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(verificationMapper).insert(verificationCaptor.capture());
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals("PENDING", result.verificationStatus());
        assertEquals(7L, verificationCaptor.getValue().getUserId());
        assertEquals("张三", verificationCaptor.getValue().getRealName());
        assertEquals("student-cards/7.jpg", verificationCaptor.getValue().getStudentCardImage());
        assertEquals(0, verificationCaptor.getValue().getStatus());
        assertEquals(1, userCaptor.getValue().getStatus());
    }

    @Test
    void submitRejectsMissingUser() {
        when(userMapper.selectActiveById(7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> verificationService.submit(
                        new VerificationSubmitRequest(7L, "张三", "20260001", "软件学院", "student-cards/7.jpg")
                )
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void submitRejectsDuplicatePendingVerification() {
        when(userMapper.selectActiveById(7L)).thenReturn(new User());
        when(verificationMapper.selectPendingByUserId(7L)).thenReturn(new UserVerification());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> verificationService.submit(
                        new VerificationSubmitRequest(7L, "张三", "20260001", "软件学院", "student-cards/7.jpg")
                )
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void reviewApprovesLatestVerificationAndMarksUserNormal() {
        UserVerification verification = new UserVerification();
        verification.setId(11L);
        verification.setUserId(7L);
        User user = new User();
        user.setId(7L);
        user.setStatus(1);
        when(verificationMapper.selectLatestByUserId(7L)).thenReturn(verification);
        when(userMapper.selectById(7L)).thenReturn(user);

        AdminVerifyResultDTO result = verificationService.review(
                7L,
                1L,
                new AdminVerifyRequest(true, "材料真实")
        );

        ArgumentCaptor<UserVerification> verificationCaptor = ArgumentCaptor.forClass(UserVerification.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(verificationMapper).updateById(verificationCaptor.capture());
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals(11L, verificationCaptor.getValue().getId());
        assertEquals(1L, verificationCaptor.getValue().getReviewerId());
        assertEquals(1, verificationCaptor.getValue().getStatus());
        assertEquals(0, userCaptor.getValue().getStatus());
        assertEquals("APPROVED", result.verificationStatus());
    }

    @Test
    void reviewRejectsLatestVerificationAndKeepsUserPending() {
        UserVerification verification = new UserVerification();
        verification.setId(11L);
        verification.setUserId(7L);
        User user = new User();
        user.setId(7L);
        user.setStatus(1);
        when(verificationMapper.selectLatestByUserId(7L)).thenReturn(verification);
        when(userMapper.selectById(7L)).thenReturn(user);

        AdminVerifyResultDTO result = verificationService.review(
                7L,
                1L,
                new AdminVerifyRequest(false, "照片不清晰")
        );

        ArgumentCaptor<UserVerification> verificationCaptor = ArgumentCaptor.forClass(UserVerification.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(verificationMapper).updateById(verificationCaptor.capture());
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals(2, verificationCaptor.getValue().getStatus());
        assertEquals("照片不清晰", verificationCaptor.getValue().getRejectReason());
        assertEquals(1, userCaptor.getValue().getStatus());
        assertEquals("REJECTED", result.verificationStatus());
    }

    @Test
    void reviewRejectsMissingVerification() {
        when(verificationMapper.selectLatestByUserId(7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> verificationService.review(7L, 1L, new AdminVerifyRequest(true, "材料真实"))
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }
}
