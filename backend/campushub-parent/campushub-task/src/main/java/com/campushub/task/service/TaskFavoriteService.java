package com.campushub.task.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campushub.common.response.PageResponse;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.task.dto.TaskFavoriteResultDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.entity.TaskFavorite;
import com.campushub.task.mapper.TaskFavoriteMapper;
import com.campushub.task.mapper.TaskMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskFavoriteService {

    private final TaskFavoriteMapper taskFavoriteMapper;
    private final TaskMapper taskMapper;
    private final TaskService taskService;

    public TaskFavoriteService(
            TaskFavoriteMapper taskFavoriteMapper,
            TaskMapper taskMapper,
            TaskService taskService
    ) {
        this.taskFavoriteMapper = taskFavoriteMapper;
        this.taskMapper = taskMapper;
        this.taskService = taskService;
    }

    @Transactional
    public TaskFavoriteResultDTO favorite(Long taskId, Long userId) {
        taskService.requireTask(taskId);
        TaskFavorite favorite = new TaskFavorite();
        favorite.setTaskId(taskId);
        favorite.setUserId(userId);
        try {
            taskFavoriteMapper.insert(favorite);
        } catch (DuplicateKeyException ignored) {
            // idempotent favorite
        }
        return new TaskFavoriteResultDTO(taskId, true);
    }

    @Transactional
    public TaskFavoriteResultDTO unfavorite(Long taskId, Long userId) {
        ValidateUtils.requirePositive(taskId, "taskId");
        taskFavoriteMapper.delete(new LambdaUpdateWrapper<TaskFavorite>()
                .eq(TaskFavorite::getTaskId, taskId)
                .eq(TaskFavorite::getUserId, userId));
        return new TaskFavoriteResultDTO(taskId, false);
    }

    public PageResponse<TaskListDTO> favorites(
            Long userId,
            String category,
            String keyword,
            String sort,
            Integer page,
            Integer size
    ) {
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        Integer categoryCode = category == null || category.isBlank() ? null : TaskCodecs.categoryCode(category);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedSort = TaskCodecs.sortKey(sort);
        int offset = (normalizedPage - 1) * normalizedSize;
        var records = taskMapper.selectFavoriteTaskList(
                userId,
                categoryCode,
                normalizedKeyword,
                normalizedSort,
                offset,
                normalizedSize
        );
        long total = taskMapper.countFavoriteTaskList(userId, categoryCode, normalizedKeyword);
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }
}
