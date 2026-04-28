package com.campushub.task.service.impl;

import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskUpdateRequest;
import com.campushub.task.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

    @Override
    public void createTask(TaskCreateRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public TaskDetailDTO getTaskDetail(Long taskId) {
        return TaskDetailDTO.builder()
                .id(taskId)
                .status("DRAFT")
                .build();
    }

    @Override
    public void updateTask(Long taskId, TaskUpdateRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }
}