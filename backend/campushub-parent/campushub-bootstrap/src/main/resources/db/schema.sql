-- CampusHub PostgreSQL schema.
-- Target database: PostgreSQL 15+ with PostGIS.

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS u_user (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL,
    password VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    nickname VARCHAR(64),
    real_name VARCHAR(64),
    department VARCHAR(64),
    avatar_url VARCHAR(255),
    role SMALLINT NOT NULL DEFAULT 0,
    credit_score INTEGER NOT NULL DEFAULT 100,
    status SMALLINT NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_u_user_student_id UNIQUE (student_id),
    CONSTRAINT ck_u_user_role CHECK (role IN (0, 1)),
    CONSTRAINT ck_u_user_credit_score CHECK (credit_score BETWEEN 0 AND 100),
    CONSTRAINT ck_u_user_status CHECK (status IN (0, 1, 2))
);

COMMENT ON TABLE u_user IS 'User account and public profile';
COMMENT ON COLUMN u_user.student_id IS 'Student id used for login';
COMMENT ON COLUMN u_user.password IS 'BCrypt password hash';
COMMENT ON COLUMN u_user.email IS 'Contact email for profile editing';
COMMENT ON COLUMN u_user.phone IS 'Contact phone number for profile editing';
COMMENT ON COLUMN u_user.real_name IS 'Real name stored in editable profile';
COMMENT ON COLUMN u_user.department IS 'College or department for public profile';
COMMENT ON COLUMN u_user.role IS '0=user, 1=admin';
COMMENT ON COLUMN u_user.status IS '0=normal, 1=pending verification, 2=banned';

CREATE TABLE IF NOT EXISTS t_user_verification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES u_user(id),
    reviewer_id BIGINT REFERENCES u_user(id),
    real_name VARCHAR(64) NOT NULL,
    student_card_image VARCHAR(255) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    reject_reason VARCHAR(255),
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_verification_status CHECK (status IN (0, 1, 2))
);

COMMENT ON TABLE t_user_verification IS 'Real-name verification submissions';
COMMENT ON COLUMN t_user_verification.status IS '0=pending, 1=approved, 2=rejected';

CREATE TABLE IF NOT EXISTS t_task (
    id BIGSERIAL PRIMARY KEY,
    publisher_id BIGINT NOT NULL REFERENCES u_user(id),
    title VARCHAR(128) NOT NULL,
    description TEXT,
    category SMALLINT NOT NULL,
    location VARCHAR(128),
    location_point GEOGRAPHY(Point, 4326),
    reward NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    deadline_time TIMESTAMPTZ,
    item_image_url VARCHAR(512),
    original_price NUMERIC(10, 2),
    team_total_members INTEGER,
    team_current_members INTEGER,
    activity_time TIMESTAMPTZ,
    activity_note TEXT,
    status SMALLINT NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_task_category CHECK (category IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_task_reward CHECK (reward >= 0),
    CONSTRAINT ck_task_original_price CHECK (original_price IS NULL OR original_price >= 0),
    CONSTRAINT ck_task_team_members CHECK (
        (team_total_members IS NULL AND team_current_members IS NULL)
        OR (
            team_total_members IS NOT NULL
            AND team_current_members IS NOT NULL
            AND team_total_members > 0
            AND team_current_members >= 0
            AND team_current_members <= team_total_members
        )
    ),
    CONSTRAINT ck_task_status CHECK (status IN (0, 1, 2, 3, 4))
);

ALTER TABLE t_task ADD COLUMN IF NOT EXISTS deadline_time TIMESTAMPTZ;
ALTER TABLE t_task ADD COLUMN IF NOT EXISTS item_image_url VARCHAR(512);
ALTER TABLE t_task ADD COLUMN IF NOT EXISTS original_price NUMERIC(10, 2);
ALTER TABLE t_task ADD COLUMN IF NOT EXISTS team_total_members INTEGER;
ALTER TABLE t_task ADD COLUMN IF NOT EXISTS team_current_members INTEGER;
ALTER TABLE t_task ADD COLUMN IF NOT EXISTS activity_time TIMESTAMPTZ;
ALTER TABLE t_task ADD COLUMN IF NOT EXISTS activity_note TEXT;

COMMENT ON TABLE t_task IS 'Campus help task';
COMMENT ON COLUMN t_task.category IS '0=express, 1=study, 2=second_hand, 3=team_up, 4=other';
COMMENT ON COLUMN t_task.location_point IS 'Optional PostGIS point for distance queries';
COMMENT ON COLUMN t_task.status IS '0=open, 1=pending_confirm, 2=in_progress, 3=completed, 4=canceled';
COMMENT ON COLUMN t_task.deadline_time IS 'Optional deadline for ordinary tasks';
COMMENT ON COLUMN t_task.item_image_url IS 'Second-hand item image URL';
COMMENT ON COLUMN t_task.original_price IS 'Second-hand item original price';
COMMENT ON COLUMN t_task.team_total_members IS 'Team-up required total member count';
COMMENT ON COLUMN t_task.team_current_members IS 'Team-up current member count';
COMMENT ON COLUMN t_task.activity_time IS 'Team-up activity time';
COMMENT ON COLUMN t_task.activity_note IS 'Team-up activity notes or requirements';

CREATE TABLE IF NOT EXISTS t_task_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES u_user(id),
    task_id BIGINT NOT NULL REFERENCES t_task(id),
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_task_favorite_user_task UNIQUE (user_id, task_id)
);

COMMENT ON TABLE t_task_favorite IS 'User favorite tasks';

CREATE TABLE IF NOT EXISTS t_order (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES t_task(id),
    poster_id BIGINT NOT NULL REFERENCES u_user(id),
    helper_id BIGINT NOT NULL REFERENCES u_user(id),
    status SMALLINT NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_order_status CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_order_version CHECK (version >= 0),
    CONSTRAINT ck_order_different_users CHECK (poster_id <> helper_id)
);

COMMENT ON TABLE t_order IS 'Task order lifecycle';
COMMENT ON COLUMN t_order.status IS '0=grabbed, 1=confirmed, 2=completed, 3=canceled, 4=disputed';
COMMENT ON COLUMN t_order.version IS 'Optimistic lock version used with Redisson grab-order lock';

ALTER TABLE t_order DROP CONSTRAINT IF EXISTS uk_order_task;

CREATE TABLE IF NOT EXISTS t_review (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES t_order(id),
    reviewer_id BIGINT NOT NULL REFERENCES u_user(id),
    target_user_id BIGINT NOT NULL REFERENCES u_user(id),
    rating SMALLINT NOT NULL,
    content VARCHAR(500),
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_review_order_reviewer UNIQUE (order_id, reviewer_id),
    CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_review_different_users CHECK (reviewer_id <> target_user_id)
);

COMMENT ON TABLE t_review IS 'Mutual order reviews';

CREATE TABLE IF NOT EXISTS t_message (
    id BIGSERIAL PRIMARY KEY,
    receiver_id BIGINT NOT NULL REFERENCES u_user(id),
    type SMALLINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_message_type CHECK (type IN (0, 1, 2, 3, 4))
);

COMMENT ON TABLE t_message IS 'System notification messages';
COMMENT ON COLUMN t_message.type IS '0=system, 1=task, 2=order, 3=review, 4=report';

CREATE TABLE IF NOT EXISTS t_chat_message (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES t_order(id),
    sender_id BIGINT NOT NULL REFERENCES u_user(id),
    receiver_id BIGINT NOT NULL REFERENCES u_user(id),
    content VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_chat_different_users CHECK (sender_id <> receiver_id)
);

COMMENT ON TABLE t_chat_message IS 'Order chat messages';

CREATE TABLE IF NOT EXISTS t_notice (
    id BIGSERIAL PRIMARY KEY,
    publisher_id BIGINT NOT NULL REFERENCES u_user(id),
    title VARCHAR(128) NOT NULL,
    content TEXT,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_notice IS 'System notices published by admin';

CREATE TABLE IF NOT EXISTS t_post (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES u_user(id),
    title VARCHAR(128) NOT NULL,
    content TEXT,
    category SMALLINT NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    is_top BOOLEAN NOT NULL DEFAULT FALSE,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_post_category CHECK (category IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_post_count_non_negative CHECK (
        view_count >= 0 AND like_count >= 0 AND comment_count >= 0
    )
);

COMMENT ON TABLE t_post IS 'Forum posts';
COMMENT ON COLUMN t_post.category IS '0=help, 1=experience, 2=secondhand, 3=lost_found, 4=other';

CREATE TABLE IF NOT EXISTS t_comment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES t_post(id),
    author_id BIGINT NOT NULL REFERENCES u_user(id),
    reply_to_comment_id BIGINT REFERENCES t_comment(id),
    content VARCHAR(1000) NOT NULL,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE t_comment IS 'Forum comments and nested replies';

CREATE TABLE IF NOT EXISTS t_post_like (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES u_user(id),
    post_id BIGINT NOT NULL REFERENCES t_post(id),
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_post_like_user_post UNIQUE (user_id, post_id)
);

COMMENT ON TABLE t_post_like IS 'Post likes';

CREATE TABLE IF NOT EXISTS t_post_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES u_user(id),
    post_id BIGINT NOT NULL REFERENCES t_post(id),
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_post_favorite_user_post UNIQUE (user_id, post_id)
);

COMMENT ON TABLE t_post_favorite IS 'User favorite posts';

CREATE TABLE IF NOT EXISTS t_report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES u_user(id),
    target_user_id BIGINT NOT NULL REFERENCES u_user(id),
    handler_id BIGINT REFERENCES u_user(id),
    task_id BIGINT REFERENCES t_task(id),
    order_id BIGINT REFERENCES t_order(id),
    post_id BIGINT REFERENCES t_post(id),
    comment_id BIGINT REFERENCES t_comment(id),
    target_type SMALLINT NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    result VARCHAR(255),
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_report_target_type CHECK (target_type IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_report_status CHECK (status IN (0, 1, 2))
);

COMMENT ON TABLE t_report IS 'Reports and moderation records';
COMMENT ON COLUMN t_report.target_type IS '0=user, 1=task, 2=order, 3=post, 4=comment';
COMMENT ON COLUMN t_report.status IS '0=pending, 1=handled, 2=rejected';

CREATE INDEX IF NOT EXISTS idx_user_role_status ON u_user (role, status);
CREATE INDEX IF NOT EXISTS idx_user_create_time ON u_user (create_time DESC);

CREATE INDEX IF NOT EXISTS idx_verification_user ON t_user_verification (user_id);
CREATE INDEX IF NOT EXISTS idx_verification_reviewer_status ON t_user_verification (reviewer_id, status);
CREATE INDEX IF NOT EXISTS idx_verification_status_time ON t_user_verification (status, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_task_publisher_status ON t_task (publisher_id, status);
CREATE INDEX IF NOT EXISTS idx_task_status_category_time ON t_task (status, category, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_task_reward ON t_task (reward);
CREATE INDEX IF NOT EXISTS idx_task_location_point ON t_task USING GIST (location_point);
CREATE INDEX IF NOT EXISTS idx_task_title_trgm ON t_task USING GIN (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_task_description_trgm ON t_task USING GIN (description gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_task_favorite_task ON t_task_favorite (task_id);
CREATE INDEX IF NOT EXISTS idx_task_favorite_user_time ON t_task_favorite (user_id, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_order_poster_status ON t_order (poster_id, status);
CREATE INDEX IF NOT EXISTS idx_order_helper_status ON t_order (helper_id, status);
CREATE INDEX IF NOT EXISTS idx_order_status_time ON t_order (status, create_time DESC);
CREATE UNIQUE INDEX IF NOT EXISTS uk_order_task_helper ON t_order (task_id, helper_id) WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_review_order ON t_review (order_id);
CREATE INDEX IF NOT EXISTS idx_review_target_time ON t_review (target_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_review_reviewer_time ON t_review (reviewer_id, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_message_receiver_read ON t_message (receiver_id, is_read);
CREATE INDEX IF NOT EXISTS idx_message_receiver_time ON t_message (receiver_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_message_type_time ON t_message (type, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_chat_order_time ON t_chat_message (order_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_chat_receiver_read ON t_chat_message (receiver_id, is_read);

CREATE INDEX IF NOT EXISTS idx_notice_time ON t_notice (create_time DESC);

CREATE INDEX IF NOT EXISTS idx_post_author_time ON t_post (author_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_post_category_time ON t_post (category, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_post_priority_time ON t_post (is_top DESC, is_recommended DESC, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_post_recommended_time ON t_post (is_recommended, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_post_title_trgm ON t_post USING GIN (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_post_content_trgm ON t_post USING GIN (content gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_comment_post_time ON t_comment (post_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_comment_author_time ON t_comment (author_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_comment_reply_to ON t_comment (reply_to_comment_id);

CREATE INDEX IF NOT EXISTS idx_post_like_post ON t_post_like (post_id);
CREATE INDEX IF NOT EXISTS idx_post_like_user_time ON t_post_like (user_id, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_post_favorite_post ON t_post_favorite (post_id);
CREATE INDEX IF NOT EXISTS idx_post_favorite_user_time ON t_post_favorite (user_id, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_report_reporter_time ON t_report (reporter_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_report_target_user_status ON t_report (target_user_id, status);
CREATE INDEX IF NOT EXISTS idx_report_handler_status ON t_report (handler_id, status);
CREATE INDEX IF NOT EXISTS idx_report_target ON t_report (target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_report_status_time ON t_report (status, create_time DESC);

-- Development seed admin account:
-- student_id: admin
-- initial password: CampusHub123
-- The password below is a BCrypt hash and must be changed before production use.
INSERT INTO u_user (
    student_id,
    password,
    nickname,
    avatar_url,
    role,
    credit_score,
    status,
    is_deleted
) VALUES (
    'admin',
    '$2a$10$PomPUVrcJgrqSwBGai29P.ct1YOZOmOYqpKzDGNjbPQUqs4r4N0V2',
    'CampusHub Admin',
    NULL,
    1,
    100,
    0,
    FALSE
) ON CONFLICT (student_id) DO NOTHING;
