package com.campushub.task.mapper;

import com.campushub.task.dto.TaskCommentDTO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TaskCommentMapper {

    @Select("""
            SELECT c.id AS commentId, c.task_id AS taskId, c.author_id AS authorId,
                   COALESCE(u.nickname, u.student_id) AS authorName, u.avatar_url AS authorAvatarUrl,
                   c.content, c.create_time AS createdAt
            FROM t_task_comment c
            JOIN u_user u ON u.id = c.author_id AND u.is_deleted = false
            WHERE c.task_id = #{taskId} AND c.is_deleted = false
            ORDER BY c.create_time DESC
            """)
    List<TaskCommentDTO> selectByTaskId(@Param("taskId") Long taskId);

    @Insert("""
            INSERT INTO t_task_comment(task_id, author_id, content)
            VALUES(#{taskId}, #{authorId}, #{content})
            """)
    int insert(@Param("taskId") Long taskId, @Param("authorId") Long authorId, @Param("content") String content);

    @Select("""
            SELECT author_id FROM t_task_comment
            WHERE id = #{commentId} AND task_id = #{taskId} AND is_deleted = false
            """)
    Long selectAuthorId(@Param("taskId") Long taskId, @Param("commentId") Long commentId);

    @Update("""
            UPDATE t_task_comment SET is_deleted = true
            WHERE id = #{commentId} AND task_id = #{taskId} AND is_deleted = false
            """)
    int softDelete(@Param("taskId") Long taskId, @Param("commentId") Long commentId);
}
