package com.campushub.task.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.task.dto.TaskCommentCreateRequest;
import com.campushub.task.dto.TaskCommentDTO;
import com.campushub.task.mapper.TaskCommentMapper;
import com.campushub.user.service.UserService;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCommentService {

    private static final int MAX_CONTENT_LENGTH = 500;
    private final TaskCommentMapper taskCommentMapper;
    private final TaskService taskService;
    private final UserService userService;

    public TaskCommentService(TaskCommentMapper taskCommentMapper, TaskService taskService, UserService userService) {
        this.taskCommentMapper = taskCommentMapper;
        this.taskService = taskService;
        this.userService = userService;
    }

    public List<TaskCommentDTO> list(Long taskId, Long viewerId, String sort) {
        taskService.requireTask(taskId);
        return isLikeSort(sort)
                ? taskCommentMapper.selectByTaskIdOrderByLikes(taskId, viewerId)
                : taskCommentMapper.selectByTaskIdOrderByTime(taskId, viewerId);
    }

    @Transactional
    public List<TaskCommentDTO> create(Long taskId, Long authorId, TaskCommentCreateRequest request) {
        userService.requireOperableUser(authorId);
        taskService.requireTask(taskId);
        String content = request == null || request.content() == null ? "" : request.content().trim();
        if (content.isEmpty() || content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评论内容应为 1 到 500 个字符");
        }
        Long parentCommentId = null;
        Long replyToUserId = null;
        if (request != null && request.parentCommentId() != null) {
            TaskCommentDTO parent = taskCommentMapper.selectById(taskId, request.parentCommentId());
            if (parent == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "回复的评论不存在");
            }
            parentCommentId = parent.parentCommentId() == null ? parent.commentId() : parent.parentCommentId();
            replyToUserId = request.replyToUserId() == null ? parent.authorId() : request.replyToUserId();
        }
        taskCommentMapper.insert(taskId, authorId, parentCommentId, replyToUserId, content);
        return list(taskId, authorId, "time");
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

    @Transactional
    public void like(Long taskId, Long commentId, Long currentUserId) {
        userService.requireOperableUser(currentUserId);
        requireExistingComment(taskId, commentId);
        if (taskCommentMapper.insertLike(currentUserId, commentId) > 0) {
            taskCommentMapper.incrementLikeCount(taskId, commentId);
        }
    }

    @Transactional
    public void unlike(Long taskId, Long commentId, Long currentUserId) {
        userService.requireOperableUser(currentUserId);
        requireExistingComment(taskId, commentId);
        if (taskCommentMapper.deleteLike(currentUserId, commentId) > 0) {
            taskCommentMapper.decrementLikeCount(taskId, commentId);
        }
    }

    private void requireExistingComment(Long taskId, Long commentId) {
        taskService.requireTask(taskId);
        if (taskCommentMapper.selectById(taskId, commentId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
    }

    private boolean isLikeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return false;
        }
        String normalized = sort.trim().toLowerCase(Locale.ROOT);
        return "likes".equals(normalized) || "hot".equals(normalized);
    }
}
