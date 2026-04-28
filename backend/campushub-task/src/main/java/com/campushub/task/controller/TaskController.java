package com.campushub.task.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.dto.TaskUpdateRequest;
import com.campushub.task.service.TaskFavoriteService;
import com.campushub.task.service.TaskQueryService;
import com.campushub.task.service.TaskService;
import com.campushub.task.service.TaskStatusService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskFavoriteService taskFavoriteService;
    private final TaskQueryService taskQueryService;
    private final TaskStatusService taskStatusService;

    @PostMapping
    public ApiResponse<String> createTask(@RequestBody TaskCreateRequest request) {
        taskService.createTask(request);
        return ApiResponse.success("Task module scaffold ready", null);
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailDTO> getTaskDetail(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getTaskDetail(taskId));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<String> updateTask(@PathVariable Long taskId, @RequestBody TaskUpdateRequest request) {
        taskService.updateTask(taskId, request);
        return ApiResponse.success("Task update scaffold ready", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<TaskListDTO>> getTaskList(@RequestParam(defaultValue = "1") Long page,
                                                              @RequestParam(defaultValue = "10") Long size) {
        List<TaskListDTO> tasks = taskQueryService.listTasks();
        return ApiResponse.success(new PageResponse<>(tasks, (long) tasks.size(), page, size));
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<String> deleteTask(@PathVariable Long taskId) {
        taskStatusService.deleteTask(taskId);
        return ApiResponse.success("Task delete scaffold ready", null);
    }

    @PostMapping("/{taskId}/favorite")
    public ApiResponse<String> favoriteTask(@PathVariable Long taskId, @RequestParam Long userId) {
        taskFavoriteService.favoriteTask(userId, taskId);
        return ApiResponse.success("Task favorite scaffold ready", null);
    }

    @DeleteMapping("/{taskId}/favorite")
    public ApiResponse<String> unfavoriteTask(@PathVariable Long taskId, @RequestParam Long userId) {
        taskFavoriteService.unfavoriteTask(userId, taskId);
        return ApiResponse.success("Task unfavorite scaffold ready", null);
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<TaskListDTO>> getFavoriteTasks(@RequestParam Long userId,
                                                                   @RequestParam(defaultValue = "1") Long page,
                                                                   @RequestParam(defaultValue = "10") Long size) {
        List<TaskListDTO> tasks = taskFavoriteService.listFavoriteTasks(userId);
        return ApiResponse.success(new PageResponse<>(tasks, (long) tasks.size(), page, size));
    }
}