package com.campushub.task.service;

import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskUpdateRequest;

public interface TaskService {

    void createTask(TaskCreateRequest request);

    TaskDetailDTO getTaskDetail(Long taskId);

    void updateTask(Long taskId, TaskUpdateRequest request);
}