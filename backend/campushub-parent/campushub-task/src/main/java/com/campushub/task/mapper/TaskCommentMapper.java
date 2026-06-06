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
                   c.parent_comment_id AS parentCommentId, c.reply_to_user_id AS replyToUserId,
                   COALESCE(reply.nickname, reply.student_id) AS replyToUserName,
                   c.content, c.like_count AS likeCount,
                   CASE
                       WHEN #{viewerId,jdbcType=BIGINT} IS NULL THEN FALSE
                       ELSE EXISTS (
                           SELECT 1 FROM t_task_comment_like l
                           WHERE l.comment_id = c.id AND l.user_id = #{viewerId,jdbcType=BIGINT}
                       )
                   END AS likedByMe,
                   c.create_time AS createdAt
            FROM t_task_comment c
            JOIN u_user u ON u.id = c.author_id AND u.is_deleted = false
            LEFT JOIN u_user reply ON reply.id = c.reply_to_user_id AND reply.is_deleted = false
            WHERE c.task_id = #{taskId} AND c.is_deleted = false
            ORDER BY c.create_time DESC
            """)
    List<TaskCommentDTO> selectByTaskIdOrderByTime(@Param("taskId") Long taskId, @Param("viewerId") Long viewerId);

    @Insert("""
            INSERT INTO t_task_comment(task_id, author_id, parent_comment_id, reply_to_user_id, content)
            VALUES(#{taskId}, #{authorId}, #{parentCommentId}, #{replyToUserId}, #{content})
            """)
    int insert(
            @Param("taskId") Long taskId,
            @Param("authorId") Long authorId,
            @Param("parentCommentId") Long parentCommentId,
            @Param("replyToUserId") Long replyToUserId,
            @Param("content") String content);

    @Select("""
            SELECT c.id AS commentId, c.task_id AS taskId, c.author_id AS authorId,
                   COALESCE(u.nickname, u.student_id) AS authorName, u.avatar_url AS authorAvatarUrl,
                   c.parent_comment_id AS parentCommentId, c.reply_to_user_id AS replyToUserId,
                   COALESCE(reply.nickname, reply.student_id) AS replyToUserName,
                   c.content, c.like_count AS likeCount,
                   CASE
                       WHEN #{viewerId,jdbcType=BIGINT} IS NULL THEN FALSE
                       ELSE EXISTS (
                           SELECT 1 FROM t_task_comment_like l
                           WHERE l.comment_id = c.id AND l.user_id = #{viewerId,jdbcType=BIGINT}
                       )
                   END AS likedByMe,
                   c.create_time AS createdAt
            FROM t_task_comment c
            JOIN u_user u ON u.id = c.author_id AND u.is_deleted = false
            LEFT JOIN u_user reply ON reply.id = c.reply_to_user_id AND reply.is_deleted = false
            WHERE c.task_id = #{taskId} AND c.is_deleted = false
            ORDER BY c.like_count DESC, c.create_time DESC
            """)
    List<TaskCommentDTO> selectByTaskIdOrderByLikes(@Param("taskId") Long taskId, @Param("viewerId") Long viewerId);

    @Select("""
            SELECT c.id AS commentId, c.task_id AS taskId, c.author_id AS authorId,
                   COALESCE(u.nickname, u.student_id) AS authorName, u.avatar_url AS authorAvatarUrl,
                   c.parent_comment_id AS parentCommentId, c.reply_to_user_id AS replyToUserId,
                   COALESCE(reply.nickname, reply.student_id) AS replyToUserName,
                   c.content, c.like_count AS likeCount, FALSE AS likedByMe,
                   c.create_time AS createdAt
            FROM t_task_comment c
            JOIN u_user u ON u.id = c.author_id AND u.is_deleted = false
            LEFT JOIN u_user reply ON reply.id = c.reply_to_user_id AND reply.is_deleted = false
            WHERE c.id = #{commentId} AND c.task_id = #{taskId} AND c.is_deleted = false
            LIMIT 1
            """)
    TaskCommentDTO selectById(@Param("taskId") Long taskId, @Param("commentId") Long commentId);

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

    @Insert("""
            INSERT INTO t_task_comment_like(user_id, comment_id)
            VALUES(#{userId}, #{commentId})
            ON CONFLICT (user_id, comment_id) DO NOTHING
            """)
    int insertLike(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Delete("""
            DELETE FROM t_task_comment_like
            WHERE user_id = #{userId} AND comment_id = #{commentId}
            """)
    int deleteLike(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Update("""
            UPDATE t_task_comment
            SET like_count = like_count + 1
            WHERE id = #{commentId} AND task_id = #{taskId} AND is_deleted = false
            """)
    int incrementLikeCount(@Param("taskId") Long taskId, @Param("commentId") Long commentId);

    @Update("""
            UPDATE t_task_comment
            SET like_count = GREATEST(0, like_count - 1)
            WHERE id = #{commentId} AND task_id = #{taskId} AND is_deleted = false
            """)
    int decrementLikeCount(@Param("taskId") Long taskId, @Param("commentId") Long commentId);
}
