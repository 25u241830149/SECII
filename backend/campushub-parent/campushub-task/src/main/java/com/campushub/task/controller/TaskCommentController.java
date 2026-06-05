package com.campushub.task.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.task.dto.TaskCommentCreateRequest;
import com.campushub.task.dto.TaskCommentDTO;
import com.campushub.task.service.TaskCommentService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @GetMapping
    public ApiResponse<List<TaskCommentDTO>> list(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "time") String sort
    ) {
        return ApiResponse.success(taskCommentService.list(taskId, SecurityUtils.getCurrentUserId().orElse(null), sort));
    }

    @PostMapping
    public ApiResponse<List<TaskCommentDTO>> create(
            @PathVariable Long taskId,
            @RequestBody TaskCommentCreateRequest request
    ) {
        return ApiResponse.success(taskCommentService.create(
                taskId, SecurityUtils.getRequiredCurrentUserId(), request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(@PathVariable Long taskId, @PathVariable Long commentId) {
        taskCommentService.delete(taskId, commentId, SecurityUtils.getRequiredCurrentUserId());
        return ApiResponse.success();
    }

    @PostMapping("/{commentId}/like")
    public ApiResponse<Void> like(@PathVariable Long taskId, @PathVariable Long commentId) {
        taskCommentService.like(taskId, commentId, SecurityUtils.getRequiredCurrentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/{commentId}/like")
    public ApiResponse<Void> unlike(@PathVariable Long taskId, @PathVariable Long commentId) {
        taskCommentService.unlike(taskId, commentId, SecurityUtils.getRequiredCurrentUserId());
        return ApiResponse.success();
    }
}
