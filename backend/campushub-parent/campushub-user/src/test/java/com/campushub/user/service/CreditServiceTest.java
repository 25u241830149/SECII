package com.campushub.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.CreditDTO;
import com.campushub.user.entity.User;
import com.campushub.user.mapper.CreditMapper;
import com.campushub.user.mapper.UserMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CreditMapper creditMapper;

    @InjectMocks
    private CreditService creditService;

    @Test
    void getCreditReturnsAggregatedCreditSnapshot() {
        User user = userWithScore(95);
        when(userMapper.selectActiveById(7L)).thenReturn(user);
        when(creditMapper.countCompletedOrdersByHelper(7L)).thenReturn(4L);
        when(creditMapper.countCancelledOrdersByHelper(7L)).thenReturn(1L);
        when(creditMapper.averageReceivedRating(7L)).thenReturn(null);
        when(creditMapper.countReceivedReviews(7L)).thenReturn(3L);
        when(creditMapper.countPublishedTasks(7L)).thenReturn(2L);
        when(creditMapper.selectRecentRecords(7L, 10)).thenReturn(List.of());

        CreditDTO credit = creditService.getCredit(7L);

        assertEquals(95, credit.creditScore());
        assertEquals(CreditService.creditLevel(95), credit.creditLevel());
        assertEquals(0.8, credit.completedRate());
        assertEquals(0.2, credit.cancelledRate());
        assertEquals(3L, credit.reviewCount());
        assertEquals(2L, credit.publishedTaskCount());
        assertEquals(4L, credit.completedOrderCount());
        assertEquals(1L, credit.cancelledOrderCount());
        assertEquals(List.of(), credit.recentRecords());
    }

    @Test
    void getCreditMapsLowScoreToWarningLevel() {
        when(userMapper.selectActiveById(7L)).thenReturn(userWithScore(55));
        stubZeroMetrics();

        CreditDTO credit = creditService.getCredit(7L);

        assertEquals(CreditService.creditLevel(55), credit.creditLevel());
    }

    @Test
    void getCreditMapsBoundaryScoreEightyToReliableLevel() {
        when(userMapper.selectActiveById(7L)).thenReturn(userWithScore(80));
        stubZeroMetrics();

        CreditDTO credit = creditService.getCredit(7L);

        assertEquals(CreditService.creditLevel(80), credit.creditLevel());
    }

    @Test
    void getCreditMapsBoundaryScoreSixtyToOrdinaryLevel() {
        when(userMapper.selectActiveById(7L)).thenReturn(userWithScore(60));
        stubZeroMetrics();

        CreditDTO credit = creditService.getCredit(7L);

        assertEquals(CreditService.creditLevel(60), credit.creditLevel());
    }

    @Test
    void getCreditMapsBoundaryScoreFiftyNineToWarningLevel() {
        when(userMapper.selectActiveById(7L)).thenReturn(userWithScore(59));
        stubZeroMetrics();

        CreditDTO credit = creditService.getCredit(7L);

        assertEquals(CreditService.creditLevel(59), credit.creditLevel());
    }

    @Test
    void getCreditRejectsMissingUser() {
        when(userMapper.selectActiveById(7L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> creditService.getCredit(7L));

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    private void stubZeroMetrics() {
        when(creditMapper.countCompletedOrdersByHelper(7L)).thenReturn(0L);
        when(creditMapper.countCancelledOrdersByHelper(7L)).thenReturn(0L);
        when(creditMapper.averageReceivedRating(7L)).thenReturn(null);
        when(creditMapper.countReceivedReviews(7L)).thenReturn(0L);
        when(creditMapper.countPublishedTasks(7L)).thenReturn(0L);
        when(creditMapper.selectRecentRecords(7L, 10)).thenReturn(List.of());
    }

    private static User userWithScore(int score) {
        User user = new User();
        user.setId(7L);
        user.setCreditScore(score);
        return user;
    }
}
