package com.campushub.task.service.impl;

import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.service.TaskQueryService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskQueryServiceImpl implements TaskQueryService {

    @Override
    public List<TaskListDTO> listTasks() {
        return Collections.emptyList();
    }
}