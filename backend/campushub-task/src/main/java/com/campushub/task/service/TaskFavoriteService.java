package com.campushub.task.service;

import com.campushub.task.dto.TaskListDTO;
import java.util.List;

public interface TaskFavoriteService {

    void favoriteTask(Long userId, Long taskId);

    void unfavoriteTask(Long userId, Long taskId);

    List<TaskListDTO> listFavoriteTasks(Long userId);
}