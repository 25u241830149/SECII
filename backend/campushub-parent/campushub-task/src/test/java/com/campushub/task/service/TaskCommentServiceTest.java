package com.campushub.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.task.dto.TaskCommentCreateRequest;
import com.campushub.task.dto.TaskCommentDTO;
import com.campushub.task.mapper.TaskCommentMapper;
import com.campushub.user.service.UserService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentMapper taskCommentMapper;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskCommentService taskCommentService;

    @Test
    void listRequiresTaskAndReturnsComments() {
        List<TaskCommentDTO> comments = List.of(comment(21L, "hello"));
        when(taskCommentMapper.selectByTaskIdOrderByTime(11L, 7L)).thenReturn(comments);

        List<TaskCommentDTO> result = taskCommentService.list(11L, 7L, "time");

        verify(taskService).requireTask(11L);
        assertEquals(comments, result);
    }

    @Test
    void createTrimsContentAndReturnsFreshList() {
        List<TaskCommentDTO> comments = List.of(comment(21L, "hello"));
        when(taskCommentMapper.selectByTaskIdOrderByTime(11L, 7L)).thenReturn(comments);

        List<TaskCommentDTO> result = taskCommentService.create(
                11L, 7L, new TaskCommentCreateRequest("  hello  ", null, null));

        verify(taskCommentMapper).insert(11L, 7L, null, null, "hello");
        assertEquals(comments, result);
    }

    @Test
    void createRejectsBlankOrOversizedContent() {
        BusinessException blank = assertThrows(
                BusinessException.class,
                () -> taskCommentService.create(11L, 7L, new TaskCommentCreateRequest(" ", null, null))
        );
        BusinessException oversized = assertThrows(
                BusinessException.class,
                () -> taskCommentService.create(11L, 7L, new TaskCommentCreateRequest("x".repeat(501), null, null))
        );

        assertEquals(ErrorCode.BAD_REQUEST, blank.getCode());
        assertEquals(ErrorCode.BAD_REQUEST, oversized.getCode());
    }

    @Test
    void deleteRejectsMissingComment() {
        when(taskCommentMapper.selectAuthorId(11L, 21L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskCommentService.delete(11L, 21L, 7L)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void deleteRejectsDeletingAnotherUsersComment() {
        when(taskCommentMapper.selectAuthorId(11L, 21L)).thenReturn(9L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> taskCommentService.delete(11L, 21L, 7L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    @Test
    void deleteSoftDeletesOwnedComment() {
        when(taskCommentMapper.selectAuthorId(11L, 21L)).thenReturn(7L);

        taskCommentService.delete(11L, 21L, 7L);

        verify(taskCommentMapper).softDelete(11L, 21L);
    }

    private static TaskCommentDTO comment(Long commentId, String content) {
        return new TaskCommentDTO(
                commentId,
                11L,
                7L,
                "Alice",
                null,
                null,
                null,
                null,
                content,
                0,
                false,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
