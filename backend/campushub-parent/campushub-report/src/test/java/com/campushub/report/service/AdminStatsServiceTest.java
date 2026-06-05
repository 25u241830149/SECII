package com.campushub.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.campushub.report.dto.AdminDashboardStatsDTO;
import com.campushub.report.mapper.AdminStatsMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private AdminStatsMapper adminStatsMapper;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    void dashboardReturnsMapperSnapshot() {
        AdminDashboardStatsDTO stats = dashboardStats();
        when(adminStatsMapper.selectDashboardStats()).thenReturn(stats);

        assertEquals(stats, adminStatsService.dashboard());
    }

    private static AdminDashboardStatsDTO dashboardStats() {
        return new AdminDashboardStatsDTO(
                100L, 4L, 2L, 20L, 8L, 50L, 30L, 18L, 3L, 12L, 7L, 45L, new BigDecimal("4.60")
        );
    }
}
