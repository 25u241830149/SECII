package com.campushub.task.service;

import com.campushub.common.audit.SensitiveWordFilter;
import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskUpdateRequest;
import com.campushub.task.entity.Task;
import com.campushub.task.event.TaskCancelledEvent;
import com.campushub.task.mapper.TaskMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final int MAX_TITLE_LENGTH = 128;
    private static final int MAX_LOCATION_LENGTH = 128;
    private static final int MAX_URL_LENGTH = 512;
    private static final int MAX_ACTIVITY_NOTE_LENGTH = 800;

    private final TaskMapper taskMapper;
    private final TaskQueryService taskQueryService;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TaskService(
            TaskMapper taskMapper,
            TaskQueryService taskQueryService,
            SensitiveWordFilter sensitiveWordFilter,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.taskMapper = taskMapper;
        this.taskQueryService = taskQueryService;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public TaskDetailDTO create(Long publisherId, TaskCreateRequest request) {
        Task task = new Task();
        task.setPublisherId(publisherId);
        fillUpdatableFields(task, request.title(), request.description(), request.category(), request.location(),
                request.reward(), request.longitude(), request.latitude(), request.deadlineTime(),
                request.itemImageUrl(), request.originalPrice(), request.teamTotalMembers(),
                request.teamCurrentMembers(), request.activityTime(), request.activityNote());
        task.setStatus(initialStatus(task));
        taskMapper.insertTask(task);
        return taskQueryService.getDetail(task.getId(), publisherId);
    }

    @Transactional
    public TaskDetailDTO update(Long taskId, Long publisherId, TaskUpdateRequest request) {
        Task existing = requireOwnedEditableTask(taskId, publisherId);
        fillUpdatableFields(existing, request.title(), request.description(), request.category(), request.location(),
                request.reward(), request.longitude(), request.latitude(), request.deadlineTime(),
                request.itemImageUrl(), request.originalPrice(), request.teamTotalMembers(),
                request.teamCurrentMembers(), request.activityTime(), request.activityNote());
        existing.setStatus(initialStatus(existing));
        taskMapper.updateTask(existing);
        return taskQueryService.getDetail(taskId, publisherId);
    }

    @Transactional
    public void delete(Long taskId, Long publisherId) {
        Task existing = requireOwnedTask(taskId, publisherId);
        if (existing.getStatus() != null && existing.getStatus() == TaskCodecs.TASK_STATUS_COMPLETED) {
            throw new BusinessException(ErrorCode.CONFLICT, "已完成需求不能取消");
        }
        existing.setStatus(TaskCodecs.TASK_STATUS_CANCELLED);
        taskMapper.updateTask(existing);
        applicationEventPublisher.publishEvent(new TaskCancelledEvent(existing.getId(), publisherId, existing.getTitle()));
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
        Task task = requireOwnedTask(taskId, publisherId);
        if (task.getStatus() != null && task.getStatus() != TaskCodecs.TASK_STATUS_OPEN) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前任务状态不允许编辑");
        }
        return task;
    }

    private Task requireOwnedTask(Long taskId, Long publisherId) {
        ValidateUtils.requirePositive(taskId, "taskId");
        Task task = taskMapper.selectTaskByIdAndPublisher(taskId, publisherId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在或无权修改");
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
            Double latitude,
            OffsetDateTime deadlineTime,
            String itemImageUrl,
            BigDecimal originalPrice,
            Integer teamTotalMembers,
            Integer teamCurrentMembers,
            OffsetDateTime activityTime,
            String activityNote
    ) {
        String normalizedTitle = normalize(title, "标题不能为空", MAX_TITLE_LENGTH);
        String normalizedDescription = description == null ? null : description.trim();
        String normalizedLocation = nullableNormalize(location, MAX_LOCATION_LENGTH);
        String normalizedImageUrl = nullableNormalize(itemImageUrl, MAX_URL_LENGTH);
        String normalizedActivityNote = nullableNormalize(activityNote, MAX_ACTIVITY_NOTE_LENGTH);
        int categoryCode = TaskCodecs.categoryCode(category);

        if (reward == null || reward.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "报酬金额不合法");
        }
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "物品原价不能小于 0");
        }
        if ((longitude == null) != (latitude == null)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "经纬度必须同时提供");
        }
        if (categoryCode == TaskCodecs.TASK_CATEGORY_TEAM_UP) {
            if (teamTotalMembers == null || teamTotalMembers <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "团队总人数必须大于 0");
            }
            if (teamCurrentMembers == null || teamCurrentMembers < 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "当前成员人数不能小于 0");
            }
            if (teamCurrentMembers > teamTotalMembers) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "当前成员人数不能超过团队总人数");
            }
        }

        sensitiveWordFilter.validate(normalizedTitle);
        if (ValidateUtils.isNotBlank(normalizedDescription)) {
            sensitiveWordFilter.validate(normalizedDescription);
        }
        if (ValidateUtils.isNotBlank(normalizedActivityNote)) {
            sensitiveWordFilter.validate(normalizedActivityNote);
        }

        task.setTitle(normalizedTitle);
        task.setDescription(normalizedDescription);
        task.setCategory(categoryCode);
        task.setLocation(normalizedLocation);
        task.setReward(reward);
        task.setLongitude(longitude);
        task.setLatitude(latitude);
        task.setDeadlineTime(deadlineTime);
        task.setItemImageUrl(categoryCode == TaskCodecs.TASK_CATEGORY_SECOND_HAND ? normalizedImageUrl : null);
        task.setOriginalPrice(categoryCode == TaskCodecs.TASK_CATEGORY_SECOND_HAND ? originalPrice : null);
        task.setTeamTotalMembers(categoryCode == TaskCodecs.TASK_CATEGORY_TEAM_UP ? teamTotalMembers : null);
        task.setTeamCurrentMembers(categoryCode == TaskCodecs.TASK_CATEGORY_TEAM_UP ? teamCurrentMembers : null);
        task.setActivityTime(categoryCode == TaskCodecs.TASK_CATEGORY_TEAM_UP ? activityTime : null);
        task.setActivityNote(categoryCode == TaskCodecs.TASK_CATEGORY_TEAM_UP ? normalizedActivityNote : null);
    }

    private static int initialStatus(Task task) {
        if (task.getCategory() != null && task.getCategory() == TaskCodecs.TASK_CATEGORY_TEAM_UP) {
            Integer total = task.getTeamTotalMembers();
            Integer current = task.getTeamCurrentMembers();
            if (total != null && current != null && current >= total) {
                return TaskCodecs.TASK_STATUS_COMPLETED;
            }
            return TaskCodecs.TASK_STATUS_IN_PROGRESS;
        }
        return TaskCodecs.TASK_STATUS_OPEN;
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
