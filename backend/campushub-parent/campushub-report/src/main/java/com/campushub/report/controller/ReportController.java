package com.campushub.report.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.report.dto.ReportCreateRequest;
import com.campushub.report.dto.ReportDTO;
import com.campushub.report.dto.ReportHandleRequest;
import com.campushub.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/reports")
    public ApiResponse<ReportDTO> create(@RequestBody ReportCreateRequest request) {
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(reportService.create(currentUserId, request));
    }

    @GetMapping("/api/admin/reports")
    public ApiResponse<PageResponse<ReportDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(reportService.list(status, page, size));
    }

    @PostMapping("/api/admin/reports/{reportId}/handle")
    public ApiResponse<ReportDTO> handle(
            @PathVariable Long reportId,
            @RequestBody ReportHandleRequest request
    ) {
        SecurityUtils.requireAdmin();
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(reportService.handle(reportId, currentUserId, request));
    }
}
