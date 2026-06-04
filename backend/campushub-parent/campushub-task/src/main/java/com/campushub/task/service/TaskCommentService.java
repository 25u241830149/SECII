package com.campushub.task.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.task.dto.TaskCommentCreateRequest;
import com.campushub.task.dto.TaskCommentDTO;
import com.campushub.task.mapper.TaskCommentMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCommentService {

    private static final int MAX_CONTENT_LENGTH = 500;
    private final TaskCommentMapper taskCommentMapper;
    private final TaskService taskService;

    public TaskCommentService(TaskCommentMapper taskCommentMapper, TaskService taskService) {
        this.taskCommentMapper = taskCommentMapper;
        this.taskService = taskService;
    }

    public List<TaskCommentDTO> list(Long taskId) {
        taskService.requireTask(taskId);
        return taskCommentMapper.selectByTaskId(taskId);
    }

    @Transactional
    public List<TaskCommentDTO> create(Long taskId, Long authorId, TaskCommentCreateRequest request) {
        taskService.requireTask(taskId);
        String content = request == null || request.content() == null ? "" : request.content().trim();
        if (content.isEmpty() || content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评论内容应为 1 到 500 个字符");
        }
        taskCommentMapper.insert(taskId, authorId, content);
        return taskCommentMapper.selectByTaskId(taskId);
    }

    @Transactional
    public void delete(Long taskId, Long commentId, Long currentUserId) {
        taskService.requireTask(taskId);
        Long authorId = taskCommentMapper.selectAuthorId(taskId, commentId);
        if (authorId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        if (!Objects.equals(authorId, currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己的评论");
        }
        taskCommentMapper.softDelete(taskId, commentId);
    }
}
