package com.campushub.task.service;

import com.campushub.task.dto.TaskListDTO;
import java.util.List;

public interface TaskQueryService {

    List<TaskListDTO> listTasks();
}