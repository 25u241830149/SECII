package com.campushub.task.service.impl;

import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.service.TaskFavoriteService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskFavoriteServiceImpl implements TaskFavoriteService {

    @Override
    public void favoriteTask(Long userId, Long taskId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void unfavoriteTask(Long userId, Long taskId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public List<TaskListDTO> listFavoriteTasks(Long userId) {
        return Collections.emptyList();
    }
}