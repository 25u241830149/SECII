package com.campushub.task.mapper;

import com.campushub.task.dto.TaskDetailDTO;
import com.campushub.task.dto.TaskListDTO;
import com.campushub.task.dto.TaskStatsDTO;
import com.campushub.task.entity.Task;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaskMapper {

    int insertTask(Task task);

    int updateTask(Task task);

    int softDeleteOrdersByTask(@Param("taskId") Long taskId);

    Task selectTaskById(@Param("taskId") Long taskId);

    Task selectTaskByIdAndPublisher(@Param("taskId") Long taskId, @Param("publisherId") Long publisherId);

    TaskDetailDTO selectTaskDetail(@Param("taskId") Long taskId, @Param("viewerId") Long viewerId);

    List<TaskListDTO> selectTaskList(
            @Param("category") Integer category,
            @Param("keyword") String keyword,
            @Param("publisherId") Long publisherId,
            @Param("viewerId") Long viewerId,
            @Param("sort") String sort,
            @Param("status") String status,
            @Param("rewardType") String rewardType,
            @Param("locationType") String locationType,
            @Param("excludeCompleted") boolean excludeCompleted,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countTaskList(
            @Param("category") Integer category,
            @Param("keyword") String keyword,
            @Param("publisherId") Long publisherId,
            @Param("status") String status,
            @Param("rewardType") String rewardType,
            @Param("locationType") String locationType,
            @Param("excludeCompleted") boolean excludeCompleted
    );

    List<TaskListDTO> selectFavoriteTaskList(
            @Param("userId") Long userId,
            @Param("category") Integer category,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countFavoriteTaskList(
            @Param("userId") Long userId,
            @Param("category") Integer category,
            @Param("keyword") String keyword
    );

    int updateTaskStatus(
            @Param("taskId") Long taskId,
            @Param("newStatus") Integer newStatus,
            @Param("currentStatus") Integer currentStatus
    );

    int forceUpdateTaskStatus(@Param("taskId") Long taskId, @Param("newStatus") Integer newStatus);

    int incrementTeamCurrentMembers(@Param("taskId") Long taskId);

    int decrementTeamCurrentMembers(@Param("taskId") Long taskId);

    long countPublishedByUser(@Param("userId") Long userId);

    TaskStatsDTO selectTaskStats();
}
