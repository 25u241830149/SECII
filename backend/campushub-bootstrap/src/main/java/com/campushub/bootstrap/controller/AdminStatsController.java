package com.campushub.bootstrap.controller;

import com.campushub.bootstrap.dto.AdminStatsDTO;
import com.campushub.bootstrap.service.StatsQueryService;
import com.campushub.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsQueryService statsQueryService;

    @GetMapping("/dashboard")
    public ApiResponse<AdminStatsDTO> getDashboardStats() {
        return ApiResponse.success(statsQueryService.getDashboardStats());
    }
}