package com.campushub.common.testing;

import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;

public final class DatabaseFixtureHelper {

    public static final String PASSWORD_HASH =
            "$2a$10$PomPUVrcJgrqSwBGai29P.ct1YOZOmOYqpKzDGNjbPQUqs4r4N0V2";

    private DatabaseFixtureHelper() {
    }

    public static Long insertUser(JdbcTemplate jdbcTemplate, String studentId, String nickname) {
        return insertUser(jdbcTemplate, studentId, nickname, 0, 0, 90);
    }

    public static Long insertUser(
            JdbcTemplate jdbcTemplate,
            String studentId,
            String nickname,
            int role,
            int status,
            int creditScore
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO u_user (
                    student_id,
                    password,
                    nickname,
                    real_name,
                    department,
                    role,
                    credit_score,
                    status,
                    is_deleted
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                studentId,
                PASSWORD_HASH,
                nickname,
                nickname + " Real",
                "Software",
                role,
                creditScore,
                status,
                false
        );
    }

    public static Long insertTask(JdbcTemplate jdbcTemplate, Long publisherId, String title) {
        return insertTask(jdbcTemplate, publisherId, title, 0, 0);
    }

    public static Long insertTask(
            JdbcTemplate jdbcTemplate,
            Long publisherId,
            String title,
            int category,
            int status
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO t_task (
                    publisher_id,
                    title,
                    description,
                    category,
                    location,
                    reward,
                    status,
                    is_deleted
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                publisherId,
                title,
                title + " description",
                category,
                "Dormitory",
                BigDecimal.valueOf(12.50),
                status,
                false
        );
    }

    public static Long insertOrder(
            JdbcTemplate jdbcTemplate,
            Long taskId,
            Long posterId,
            Long helperId,
            int status
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO t_order (
                    task_id,
                    poster_id,
                    helper_id,
                    status,
                    version,
                    is_deleted
                ) VALUES (?, ?, ?, ?, 0, false)
                RETURNING id
                """,
                Long.class,
                taskId,
                posterId,
                helperId,
                status
        );
    }

    public static void insertVerification(
            JdbcTemplate jdbcTemplate,
            Long userId,
            String realName,
            String studentCardImage,
            int status
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO t_user_verification (
                    user_id,
                    real_name,
                    student_card_image,
                    status
                ) VALUES (?, ?, ?, ?)
                """,
                userId,
                realName,
                studentCardImage,
                status
        );
    }

    public static Long insertMessage(
            JdbcTemplate jdbcTemplate,
            Long receiverId,
            int type,
            String title,
            String content,
            boolean read
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO t_message (
                    receiver_id,
                    type,
                    title,
                    content,
                    is_read
                ) VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                receiverId,
                type,
                title,
                content,
                read
        );
    }

    public static Long insertNotice(
            JdbcTemplate jdbcTemplate,
            Long publisherId,
            String title,
            String content
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO t_notice (
                    publisher_id,
                    title,
                    content
                ) VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                publisherId,
                title,
                content
        );
    }

    public static Long insertReport(
            JdbcTemplate jdbcTemplate,
            Long reporterId,
            Long targetUserId,
            int targetType,
            Long targetId,
            String reason,
            int status
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO t_report (
                    reporter_id,
                    target_user_id,
                    target_type,
                    target_id,
                    reason,
                    status
                ) VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                reporterId,
                targetUserId,
                targetType,
                targetId,
                reason,
                status
        );
    }
}
