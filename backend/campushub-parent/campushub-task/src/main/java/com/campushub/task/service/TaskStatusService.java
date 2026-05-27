package com.campushub.task.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.task.mapper.TaskMapper;
import org.springframework.stereotype.Service;

@Service
public class TaskStatusService {

    private final TaskMapper taskMapper;

    public TaskStatusService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public boolean lockForGrab(Long taskId) {
        return taskMapper.updateTaskStatus(taskId, TaskCodecs.TASK_STATUS_LOCKED, TaskCodecs.TASK_STATUS_OPEN) > 0;
    }

    public void markInProgress(Long taskId) {
        if (taskMapper.updateTaskStatus(taskId, TaskCodecs.TASK_STATUS_IN_PROGRESS, TaskCodecs.TASK_STATUS_LOCKED) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "任务状态已变化，无法确认订单");
        }
    }

    public void reopen(Long taskId) {
        taskMapper.forceUpdateTaskStatus(taskId, TaskCodecs.TASK_STATUS_OPEN);
    }

    public void markCompleted(Long taskId) {
        taskMapper.forceUpdateTaskStatus(taskId, TaskCodecs.TASK_STATUS_COMPLETED);
    }

    public boolean incrementTeamCurrentMembers(Long taskId) {
        return taskMapper.incrementTeamCurrentMembers(taskId) > 0;
    }

    public void markCancelled(Long taskId) {
        taskMapper.forceUpdateTaskStatus(taskId, TaskCodecs.TASK_STATUS_CANCELLED);
    }

    public void offline(Long taskId) {
        taskMapper.forceUpdateTaskStatus(taskId, TaskCodecs.TASK_STATUS_OFFLINE);
    }
}
