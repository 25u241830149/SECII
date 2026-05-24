package com.campushub.task.service;

import com.campushub.common.audit.SensitiveWordFilter;
import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskUpdateRequest;
import com.campushub.task.entity.Task;
import com.campushub.task.mapper.TaskMapper;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final int MAX_TITLE_LENGTH = 128;
    private static final int MAX_LOCATION_LENGTH = 128;

    private final TaskMapper taskMapper;
    private final TaskQueryService taskQueryService;
    private final SensitiveWordFilter sensitiveWordFilter;

    public TaskService(
            TaskMapper taskMapper,
            TaskQueryService taskQueryService,
            SensitiveWordFilter sensitiveWordFilter
    ) {
        this.taskMapper = taskMapper;
        this.taskQueryService = taskQueryService;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    @Transactional
    public TaskDetailDTO create(Long publisherId, TaskCreateRequest request) {
        Task task = new Task();
        task.setPublisherId(publisherId);
        fillUpdatableFields(task, request.title(), request.description(), request.category(), request.location(),
                request.reward(), request.longitude(), request.latitude());
        task.setStatus(TaskCodecs.TASK_STATUS_OPEN);
        taskMapper.insertTask(task);
        return taskQueryService.getDetail(task.getId(), publisherId);
    }

    @Transactional
    public TaskDetailDTO update(Long taskId, Long publisherId, TaskUpdateRequest request) {
        Task existing = requireOwnedEditableTask(taskId, publisherId);
        fillUpdatableFields(existing, request.title(), request.description(), request.category(), request.location(),
                request.reward(), request.longitude(), request.latitude());
        taskMapper.updateTask(existing);
        return taskQueryService.getDetail(taskId, publisherId);
    }

    @Transactional
    public void delete(Long taskId, Long publisherId) {
        Task existing = requireOwnedEditableTask(taskId, publisherId);
        existing.setIsDeleted(true);
        taskMapper.updateTask(existing);
    }

    @Transactional
    public void adminOffline(Long taskId) {
        Task existing = requireTask(taskId);
        existing.setStatus(TaskCodecs.TASK_STATUS_OFFLINE);
        taskMapper.updateTask(existing);
    }

    public Task requireTask(Long taskId) {
        ValidateUtils.requirePositive(taskId, "taskId");
        Task task = taskMapper.selectTaskById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        return task;
    }

    private Task requireOwnedEditableTask(Long taskId, Long publisherId) {
        ValidateUtils.requirePositive(taskId, "taskId");
        Task task = taskMapper.selectTaskByIdAndPublisher(taskId, publisherId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在或无权修改");
        }
        if (task.getStatus() != null && task.getStatus() != TaskCodecs.TASK_STATUS_OPEN) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前任务状态不允许编辑或删除");
        }
        return task;
    }

    private void fillUpdatableFields(
            Task task,
            String title,
            String description,
            String category,
            String location,
            BigDecimal reward,
            Double longitude,
            Double latitude
    ) {
        String normalizedTitle = normalize(title, "标题不能为空", MAX_TITLE_LENGTH);
        String normalizedDescription = description == null ? null : description.trim();
        String normalizedLocation = nullableNormalize(location, MAX_LOCATION_LENGTH);

        if (reward == null || reward.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "报酬金额不合法");
        }
        if ((longitude == null) != (latitude == null)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "经纬度必须同时提供");
        }

        sensitiveWordFilter.validate(normalizedTitle);
        if (ValidateUtils.isNotBlank(normalizedDescription)) {
            sensitiveWordFilter.validate(normalizedDescription);
        }

        task.setTitle(normalizedTitle);
        task.setDescription(normalizedDescription);
        task.setCategory(TaskCodecs.categoryCode(category));
        task.setLocation(normalizedLocation);
        task.setReward(reward);
        task.setLongitude(longitude);
        task.setLatitude(latitude);
    }

    private static String normalize(String value, String emptyMessage, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, emptyMessage);
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "字段长度超过限制");
        }
        return normalized;
    }

    private static String nullableNormalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "字段长度超过限制");
        }
        return normalized;
    }
}
