package com.campushub.task.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.mapper.TaskMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskQueryService {

    private final TaskMapper taskMapper;

    public TaskQueryService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public PageResponse<TaskListDTO> list(
            String category,
            String keyword,
            String sort,
            Integer page,
            Integer size,
            Long publisherId,
            Long viewerId
    ) {
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        Integer categoryCode = category == null || category.isBlank() ? null : TaskCodecs.categoryCode(category);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedSort = TaskCodecs.sortKey(sort);
        int offset = (normalizedPage - 1) * normalizedSize;

        List<TaskListDTO> records = taskMapper.selectTaskList(
                categoryCode,
                normalizedKeyword,
                publisherId,
                viewerId,
                normalizedSort,
                offset,
                normalizedSize
        );
        long total = taskMapper.countTaskList(categoryCode, normalizedKeyword, publisherId);
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }

    public TaskDetailDTO getDetail(Long taskId, Long viewerId) {
        ValidateUtils.requirePositive(taskId, "taskId");
        TaskDetailDTO detail = taskMapper.selectTaskDetail(taskId, viewerId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        return detail;
    }
}
