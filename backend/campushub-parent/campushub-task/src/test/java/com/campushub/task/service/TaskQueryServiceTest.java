package com.campushub.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.dto.TaskStatsDTO;
import com.campushub.task.mapper.TaskMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskQueryServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskQueryService taskQueryService;

    @Test
    void listNormalizesFiltersAndBuildsPageResponse() {
        TaskListDTO dto = taskList(11L, "OPEN");
        when(taskMapper.selectTaskList(
                eq(TaskCodecs.TASK_CATEGORY_EXPRESS),
                eq("keyword"),
                eq(7L),
                eq(9L),
                eq("hot"),
                eq("PENDING_CONFIRM"),
                eq("PAID"),
                eq("WITH_LOCATION"),
                eq(true),
                eq(10),
                eq(5)
        )).thenReturn(List.of(dto));
        when(taskMapper.countTaskList(
                eq(TaskCodecs.TASK_CATEGORY_EXPRESS),
                eq("keyword"),
                eq(7L),
                eq("PENDING_CONFIRM"),
                eq("PAID"),
                eq("WITH_LOCATION"),
                eq(true)
        )).thenReturn(1L);

        PageResponse<TaskListDTO> page = taskQueryService.list(
                "EXPRESS",
                " keyword ",
                "hot",
                3,
                5,
                7L,
                9L,
                "LOCKED",
                "PAID",
                "WITH_LOCATION",
                true
        );

        assertEquals(1L, page.total());
        assertEquals(3, page.page());
        assertEquals(5, page.size());
        assertEquals(dto, page.records().get(0));
    }

    @Test
    void statsReturnsMapperSnapshot() {
        TaskStatsDTO stats = new TaskStatsDTO(2, 3, 4);
        when(taskMapper.selectTaskStats()).thenReturn(stats);

        assertEquals(stats, taskQueryService.stats());
    }

    @Test
    void getDetailRejectsMissingTask() {
        when(taskMapper.selectTaskDetail(11L, 7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskQueryService.getDetail(11L, 7L)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void getDetailReturnsExistingTask() {
        TaskDetailDTO detail = new TaskDetailDTO(
                11L, "Need pickup", "desc", "EXPRESS", "OPEN", BigDecimal.TEN,
                "Dorm", null, null, null, null, null, null, null, null, null,
                7L, "Alice", null, "Software", 90, 0, false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
        when(taskMapper.selectTaskDetail(11L, 7L)).thenReturn(detail);

        assertEquals(detail, taskQueryService.getDetail(11L, 7L));
    }

    private static TaskListDTO taskList(Long taskId, String status) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-05T12:00:00+08:00");
        return new TaskListDTO(
                taskId, "Need pickup", "desc", "EXPRESS", status, BigDecimal.TEN,
                "Dorm", null, null, null, null, null, null, null, null, null,
                7L, "Alice", null, 90, 2, false, now, now
        );
    }
}
