package com.campushub.report.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.report.dto.AdminDashboardStatsDTO;
import com.campushub.report.service.AdminStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardStatsDTO> dashboard() {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(adminStatsService.dashboard());
    }
}
