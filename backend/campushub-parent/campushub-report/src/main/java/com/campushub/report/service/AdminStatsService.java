package com.campushub.report.service;

import com.campushub.report.dto.AdminDashboardStatsDTO;
import com.campushub.report.mapper.AdminStatsMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminStatsService {

    private final AdminStatsMapper adminStatsMapper;

    public AdminStatsService(AdminStatsMapper adminStatsMapper) {
        this.adminStatsMapper = adminStatsMapper;
    }

    public AdminDashboardStatsDTO dashboard() {
        return adminStatsMapper.selectDashboardStats();
    }
}
