package com.campushub.bootstrap.service.impl;

import com.campushub.bootstrap.dto.AdminStatsDTO;
import com.campushub.bootstrap.service.StatsQueryService;
import org.springframework.stereotype.Service;

@Service
public class StatsQueryServiceImpl implements StatsQueryService {

    @Override
    public AdminStatsDTO getDashboardStats() {
        return AdminStatsDTO.builder()
                .userCount(0L)
                .taskCount(0L)
                .orderCount(0L)
                .forumPostCount(0L)
                .pendingVerificationCount(0L)
                .build();
    }
}