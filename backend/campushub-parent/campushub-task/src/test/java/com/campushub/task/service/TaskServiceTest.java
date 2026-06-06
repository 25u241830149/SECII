package com.campushub.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campushub.common.audit.SensitiveWordFilter;
import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.task.dto.TaskCreateRequest;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskUpdateRequest;
import com.campushub.task.entity.Task;
import com.campushub.task.event.TaskCancelledEvent;
import com.campushub.task.mapper.TaskMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskQueryService taskQueryService;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createNormalTaskTrimsFieldsAndStartsOpen() {
        TaskDetailDTO detail = taskDetail(11L, "Need pickup", "OPEN");
        when(taskMapper.insertTask(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(11L);
            return 1;
        });
        when(taskQueryService.getDetail(11L, 7L)).thenReturn(detail);

        TaskDetailDTO result = taskService.create(
                7L,
                new TaskCreateRequest(
                        " Need pickup ",
                        " Bring package to dorm ",
                        "EXPRESS",
                        " North Gate ",
                        BigDecimal.valueOf(8.50),
                        120.123,
                        30.456,
                        OffsetDateTime.parse("2026-06-06T12:00:00+08:00"),
                        " https://unused.example/item.png ",
                        BigDecimal.valueOf(99),
                        4,
                        1,
                        OffsetDateTime.parse("2026-06-07T15:00:00+08:00"),
                        " Team note "
                )
        );

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insertTask(taskCaptor.capture());
        Task saved = taskCaptor.getValue();
        assertEquals(7L, saved.getPublisherId());
        assertEquals("Need pickup", saved.getTitle());
        assertEquals(" Bring package to dorm ".trim(), saved.getDescription());
        assertEquals(TaskCodecs.TASK_CATEGORY_EXPRESS, saved.getCategory());
        assertEquals("North Gate", saved.getLocation());
        assertEquals(BigDecimal.valueOf(8.50), saved.getReward());
        assertEquals(120.123, saved.getLongitude());
        assertEquals(30.456, saved.getLatitude());
        assertEquals(TaskCodecs.TASK_STATUS_OPEN, saved.getStatus());
        assertEquals(null, saved.getItemImageUrl());
        assertEquals(null, saved.getOriginalPrice());
        assertEquals(null, saved.getTeamTotalMembers());
        assertEquals(null, saved.getTeamCurrentMembers());
        verify(sensitiveWordFilter).validate("Need pickup");
        verify(sensitiveWordFilter).validate("Bring package to dorm");
        assertEquals(detail, result);
    }

    @Test
    void createTeamUpTaskStartsInProgressAndKeepsTeamFields() {
        when(taskMapper.insertTask(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(21L);
            return 1;
        });
        when(taskQueryService.getDetail(21L, 9L)).thenReturn(taskDetail(21L, "Study group", "IN_PROGRESS"));

        taskService.create(
                9L,
                new TaskCreateRequest(
                        "Study group",
                        "Prepare finals together",
                        "TEAM_UP",
                        "Library",
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        null,
                        null,
                        5,
                        2,
                        OffsetDateTime.parse("2026-06-10T19:00:00+08:00"),
                        " Quiet room "
                )
        );

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insertTask(taskCaptor.capture());
        Task saved = taskCaptor.getValue();
        assertEquals(TaskCodecs.TASK_CATEGORY_TEAM_UP, saved.getCategory());
        assertEquals(TaskCodecs.TASK_STATUS_IN_PROGRESS, saved.getStatus());
        assertEquals(5, saved.getTeamTotalMembers());
        assertEquals(2, saved.getTeamCurrentMembers());
        assertEquals("Quiet room", saved.getActivityNote());
    }

    @Test
    void createRejectsHalfSpecifiedCoordinates() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.create(
                        7L,
                        createRequestBuilder()
                                .longitude(120.0)
                                .latitude(null)
                                .build()
                )
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
        verifyNoInteractions(taskMapper, taskQueryService);
    }

    @Test
    void createRejectsBlankTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder().title("   ").build())
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
        verifyNoInteractions(taskMapper, taskQueryService);
    }

    @Test
    void createRejectsOverlongTextFields() {
        BusinessException title = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder().title("x".repeat(129)).build())
        );
        BusinessException location = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder().location("x".repeat(129)).build())
        );
        BusinessException imageUrl = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder()
                        .category("SECOND_HAND")
                        .itemImageUrl("x".repeat(513))
                        .originalPrice(BigDecimal.TEN)
                        .build())
        );
        BusinessException activityNote = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder()
                        .category("TEAM_UP")
                        .teamTotalMembers(4)
                        .teamCurrentMembers(1)
                        .activityNote("x".repeat(801))
                        .build())
        );

        assertEquals(ErrorCode.BAD_REQUEST, title.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, location.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, imageUrl.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, activityNote.getCode());
    }

    @Test
    void createRejectsInvalidRewardAndOriginalPrice() {
        BusinessException nullReward = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder().reward(null).build())
        );
        BusinessException negativeReward = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder().reward(BigDecimal.valueOf(-1)).build())
        );
        BusinessException negativeOriginalPrice = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder()
                        .category("SECOND_HAND")
                        .originalPrice(BigDecimal.valueOf(-1))
                        .build())
        );

        assertEquals(ErrorCode.BAD_REQUEST, nullReward.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, negativeReward.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, negativeOriginalPrice.getCode());
    }

    @Test
    void createRejectsInvalidTeamMemberConfig() {
        BusinessException missingTotal = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder()
                        .category("TEAM_UP")
                        .teamTotalMembers(null)
                        .teamCurrentMembers(1)
                        .build())
        );
        BusinessException negativeCurrent = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder()
                        .category("TEAM_UP")
                        .teamTotalMembers(4)
                        .teamCurrentMembers(-1)
                        .build())
        );
        BusinessException currentExceedsTotal = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder()
                        .category("TEAM_UP")
                        .teamTotalMembers(4)
                        .teamCurrentMembers(5)
                        .build())
        );

        assertEquals(ErrorCode.BAD_REQUEST, missingTotal.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, negativeCurrent.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, currentExceedsTotal.getCode());
    }

    @Test
    void createRejectsSensitiveWordsFromValidatedFields() {
        doThrow(new BusinessException(ErrorCode.BAD_REQUEST, "blocked"))
                .when(sensitiveWordFilter).validate("Need pickup");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.create(7L, createRequestBuilder().build())
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
        verifyNoInteractions(taskMapper, taskQueryService);
    }

    @Test
    void createCompletedTeamUpTaskWhenRosterIsFull() {
        when(taskMapper.insertTask(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(22L);
            return 1;
        });
        when(taskQueryService.getDetail(22L, 9L)).thenReturn(taskDetail(22L, "Study group", "COMPLETED"));

        taskService.create(
                9L,
                createRequestBuilder()
                        .title("Study group")
                        .description("Prepare finals together")
                        .category("TEAM_UP")
                        .location("Library")
                        .reward(BigDecimal.ZERO)
                        .teamTotalMembers(4)
                        .teamCurrentMembers(4)
                        .activityNote("Quiet room")
                        .build()
        );

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insertTask(taskCaptor.capture());
        assertEquals(TaskCodecs.TASK_STATUS_COMPLETED, taskCaptor.getValue().getStatus());
    }

    @Test
    void updateRejectsEditingNonOpenTask() {
        Task existing = new Task();
        existing.setId(11L);
        existing.setPublisherId(7L);
        existing.setStatus(TaskCodecs.TASK_STATUS_LOCKED);
        when(taskMapper.selectTaskByIdAndPublisher(11L, 7L)).thenReturn(existing);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.update(
                        11L,
                        7L,
                        new TaskUpdateRequest(
                                "Updated",
                                "desc",
                                "EXPRESS",
                                "Dorm",
                                BigDecimal.TEN,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                )
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void updateRejectsNonOwnedTask() {
        when(taskMapper.selectTaskByIdAndPublisher(11L, 7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskService.update(11L, 7L, updateRequest())
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void deleteRejectsCompletedTask() {
        Task existing = new Task();
        existing.setId(11L);
        existing.setPublisherId(7L);
        existing.setTitle("Completed");
        existing.setStatus(TaskCodecs.TASK_STATUS_COMPLETED);
        when(taskMapper.selectTaskByIdAndPublisher(11L, 7L)).thenReturn(existing);

        BusinessException exception = assertThrows(BusinessException.class, () -> taskService.delete(11L, 7L));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void deleteRejectsNonOwnedTask() {
        when(taskMapper.selectTaskByIdAndPublisher(11L, 7L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> taskService.delete(11L, 7L));

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void deleteCancelsTaskAndPublishesEvent() {
        Task existing = new Task();
        existing.setId(11L);
        existing.setPublisherId(7L);
        existing.setTitle("Need pickup");
        existing.setStatus(TaskCodecs.TASK_STATUS_OPEN);
        when(taskMapper.selectTaskByIdAndPublisher(11L, 7L)).thenReturn(existing);

        taskService.delete(11L, 7L);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateTask(taskCaptor.capture());
        assertEquals(TaskCodecs.TASK_STATUS_CANCELLED, taskCaptor.getValue().getStatus());
        verify(applicationEventPublisher).publishEvent(any(TaskCancelledEvent.class));
    }

    @Test
    void adminOfflineMarksTaskOffline() {
        Task existing = new Task();
        existing.setId(11L);
        existing.setPublisherId(7L);
        existing.setStatus(TaskCodecs.TASK_STATUS_OPEN);
        when(taskMapper.selectTaskById(11L)).thenReturn(existing);

        taskService.adminOffline(11L);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateTask(taskCaptor.capture());
        assertEquals(TaskCodecs.TASK_STATUS_OFFLINE, taskCaptor.getValue().getStatus());
    }

    @Test
    void requireTaskRejectsMissingTask() {
        when(taskMapper.selectTaskById(11L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> taskService.requireTask(11L));

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    private static TaskUpdateRequest updateRequest() {
        return new TaskUpdateRequest(
                "Updated",
                "desc",
                "EXPRESS",
                "Dorm",
                BigDecimal.TEN,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static CreateRequestBuilder createRequestBuilder() {
        return new CreateRequestBuilder();
    }

    private static TaskDetailDTO taskDetail(Long taskId, String title, String status) {
        return new TaskDetailDTO(
                taskId,
                title,
                "desc",
                "EXPRESS",
                status,
                BigDecimal.TEN,
                "Dorm",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                7L,
                "Alice",
                null,
                "Software",
                95,
                0,
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }

    private static final class CreateRequestBuilder {
        private String title = "Need pickup";
        private String description = "desc";
        private String category = "EXPRESS";
        private String location = "Dorm";
        private BigDecimal reward = BigDecimal.ONE;
        private Double longitude;
        private Double latitude;
        private OffsetDateTime deadlineTime;
        private String itemImageUrl;
        private BigDecimal originalPrice;
        private Integer teamTotalMembers;
        private Integer teamCurrentMembers;
        private OffsetDateTime activityTime;
        private String activityNote;

        private CreateRequestBuilder title(String value) {
            this.title = value;
            return this;
        }

        private CreateRequestBuilder description(String value) {
            this.description = value;
            return this;
        }

        private CreateRequestBuilder category(String value) {
            this.category = value;
            return this;
        }

        private CreateRequestBuilder location(String value) {
            this.location = value;
            return this;
        }

        private CreateRequestBuilder reward(BigDecimal value) {
            this.reward = value;
            return this;
        }

        private CreateRequestBuilder longitude(Double value) {
            this.longitude = value;
            return this;
        }

        private CreateRequestBuilder latitude(Double value) {
            this.latitude = value;
            return this;
        }

        private CreateRequestBuilder itemImageUrl(String value) {
            this.itemImageUrl = value;
            return this;
        }

        private CreateRequestBuilder originalPrice(BigDecimal value) {
            this.originalPrice = value;
            return this;
        }

        private CreateRequestBuilder teamTotalMembers(Integer value) {
            this.teamTotalMembers = value;
            return this;
        }

        private CreateRequestBuilder teamCurrentMembers(Integer value) {
            this.teamCurrentMembers = value;
            return this;
        }

        private CreateRequestBuilder activityNote(String value) {
            this.activityNote = value;
            return this;
        }

        private TaskCreateRequest build() {
            return new TaskCreateRequest(
                    title,
                    description,
                    category,
                    location,
                    reward,
                    longitude,
                    latitude,
                    deadlineTime,
                    itemImageUrl,
                    originalPrice,
                    teamTotalMembers,
                    teamCurrentMembers,
                    activityTime,
                    activityNote
            );
        }
    }
}
