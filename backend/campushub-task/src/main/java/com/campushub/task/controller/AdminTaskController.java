package com.campushub.task.controller;

import com.campushub.common.response.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tasks")
public class AdminTaskController {

    @DeleteMapping("/{taskId}")
    public ApiResponse<String> removeTask(@PathVariable Long taskId) {
        return ApiResponse.success("Admin task scaffold ready", null);
    }
}