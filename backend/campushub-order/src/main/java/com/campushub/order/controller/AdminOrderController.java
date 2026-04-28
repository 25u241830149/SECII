package com.campushub.order.controller;

import com.campushub.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminOrderController {

    @PostMapping("/{reportId}/handle")
    public ApiResponse<String> handleReport(@PathVariable Long reportId) {
        return ApiResponse.success("Admin report scaffold ready", null);
    }
}