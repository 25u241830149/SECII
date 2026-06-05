package com.campushub.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.response.PageResponse;
import com.campushub.task.dto.TaskFavoriteResultDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.entity.TaskFavorite;
import com.campushub.task.mapper.TaskFavoriteMapper;
import com.campushub.task.mapper.TaskMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class TaskFavoriteServiceTest {

    @Mock
    private TaskFavoriteMapper taskFavoriteMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskFavoriteService taskFavoriteService;

    @Test
    void favoriteIsIdempotentOnDuplicateKey() {
        doThrow(new DuplicateKeyException("duplicate")).when(taskFavoriteMapper).insert(org.mockito.ArgumentMatchers.<TaskFavorite>any());

        TaskFavoriteResultDTO result = taskFavoriteService.favorite(11L, 7L);

        verify(taskService).requireTask(11L);
        assertEquals(11L, result.taskId());
        assertEquals(true, result.favorited());
    }

    @Test
    void unfavoriteDeletesFavoriteByTaskAndUser() {
        TaskFavoriteResultDTO result = taskFavoriteService.unfavorite(11L, 7L);

        verify(taskFavoriteMapper).delete(any());
        assertEquals(false, result.favorited());
    }

    @Test
    void favoritesNormalizesFiltersAndWrapsPage() {
        TaskListDTO dto = taskList(11L);
        when(taskMapper.selectFavoriteTaskList(
                eq(7L),
                eq(TaskCodecs.TASK_CATEGORY_STUDY),
                eq("keyword"),
                eq("hot"),
                eq(5),
                eq(5)
        )).thenReturn(List.of(dto));
        when(taskMapper.countFavoriteTaskList(7L, TaskCodecs.TASK_CATEGORY_STUDY, "keyword")).thenReturn(1L);

        PageResponse<TaskListDTO> page = taskFavoriteService.favorites(7L, "STUDY", " keyword ", "hot", 2, 5);

        assertEquals(1L, page.total());
        assertEquals(2, page.page());
        assertEquals(dto, page.records().get(0));
    }

    private static TaskListDTO taskList(Long taskId) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-05T12:00:00+08:00");
        return new TaskListDTO(
                taskId, "Study together", "desc", "STUDY", "OPEN", BigDecimal.ZERO,
                "Library", null, null, null, null, null, null, null, null, null,
                7L, "Alice", null, 90, 1, true, now, now
        );
    }
}
