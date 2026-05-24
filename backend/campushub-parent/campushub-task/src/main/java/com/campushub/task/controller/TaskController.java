package com.campushub.task.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskFavoriteResultDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.dto.TaskUpdateRequest;
import com.campushub.task.service.TaskFavoriteService;
import com.campushub.task.service.TaskQueryService;
import com.campushub.task.service.TaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskQueryService taskQueryService;
    private final TaskFavoriteService taskFavoriteService;

    public TaskController(
            TaskService taskService,
            TaskQueryService taskQueryService,
            TaskFavoriteService taskFavoriteService
    ) {
        this.taskService = taskService;
        this.taskQueryService = taskQueryService;
        this.taskFavoriteService = taskFavoriteService;
    }

    @PostMapping
    public ApiResponse<TaskDetailDTO> create(@RequestBody TaskCreateRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(taskService.create(userId, request));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<TaskDetailDTO> update(@PathVariable Long taskId, @RequestBody TaskUpdateRequest request) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(taskService.update(taskId, userId, request));
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> delete(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        taskService.delete(taskId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailDTO> detail(@PathVariable Long taskId) {
        Long viewerId = SecurityUtils.getCurrentUserId().orElse(null);
        return ApiResponse.success(taskQueryService.getDetail(taskId, viewerId));
    }

    @GetMapping
    public ApiResponse<PageResponse<TaskListDTO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long publisherId
    ) {
        Long viewerId = SecurityUtils.getCurrentUserId().orElse(null);
        return ApiResponse.success(taskQueryService.list(category, keyword, sort, page, size, publisherId, viewerId));
    }

    @PostMapping("/{taskId}/favorite")
    public ApiResponse<TaskFavoriteResultDTO> favorite(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(taskFavoriteService.favorite(taskId, userId));
    }

    @DeleteMapping("/{taskId}/favorite")
    public ApiResponse<TaskFavoriteResultDTO> unfavorite(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(taskFavoriteService.unfavorite(taskId, userId));
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<TaskListDTO>> favorites(
            @RequestParam Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        SecurityUtils.requireCurrentUser(userId);
        return ApiResponse.success(taskFavoriteService.favorites(userId, category, keyword, sort, page, size));
    }
}
