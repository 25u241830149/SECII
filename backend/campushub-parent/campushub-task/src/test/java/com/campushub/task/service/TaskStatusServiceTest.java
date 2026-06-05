package com.campushub.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.task.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStatusServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskStatusService taskStatusService;

    @Test
    void lockForGrabReturnsWhetherTransitionSucceeded() {
        when(taskMapper.updateTaskStatus(11L, TaskCodecs.TASK_STATUS_LOCKED, TaskCodecs.TASK_STATUS_OPEN)).thenReturn(1);
        when(taskMapper.updateTaskStatus(12L, TaskCodecs.TASK_STATUS_LOCKED, TaskCodecs.TASK_STATUS_OPEN)).thenReturn(0);

        assertTrue(taskStatusService.lockForGrab(11L));
        assertFalse(taskStatusService.lockForGrab(12L));
    }

    @Test
    void markInProgressRejectsConflictingStateChange() {
        when(taskMapper.updateTaskStatus(11L, TaskCodecs.TASK_STATUS_IN_PROGRESS, TaskCodecs.TASK_STATUS_LOCKED))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskStatusService.markInProgress(11L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void reopenAndCompletionDelegateToForceUpdate() {
        taskStatusService.reopen(11L);
        taskStatusService.markCompleted(12L);
        taskStatusService.markCancelled(13L);
        taskStatusService.offline(14L);

        verify(taskMapper).forceUpdateTaskStatus(11L, TaskCodecs.TASK_STATUS_OPEN);
        verify(taskMapper).forceUpdateTaskStatus(12L, TaskCodecs.TASK_STATUS_COMPLETED);
        verify(taskMapper).forceUpdateTaskStatus(13L, TaskCodecs.TASK_STATUS_CANCELLED);
        verify(taskMapper).forceUpdateTaskStatus(14L, TaskCodecs.TASK_STATUS_OFFLINE);
    }

    @Test
    void teamMemberMutationsUseMapperCounters() {
        when(taskMapper.incrementTeamCurrentMembers(11L)).thenReturn(1);

        assertTrue(taskStatusService.incrementTeamCurrentMembers(11L));
        taskStatusService.decrementTeamCurrentMembers(11L);

        verify(taskMapper).decrementTeamCurrentMembers(11L);
    }
}
