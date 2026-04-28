CREATE TABLE IF NOT EXISTS u_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    nickname VARCHAR(64),
    real_name VARCHAR(64),
    department VARCHAR(100),
    avatar VARCHAR(255),
    role VARCHAR(32) NOT NULL DEFAULT 'STUDENT',
    credit_score INTEGER NOT NULL DEFAULT 100,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS t_task (
    id BIGSERIAL PRIMARY KEY,
    publisher_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    category VARCHAR(32) NOT NULL,
    reward NUMERIC(10, 2) DEFAULT 0,
    location VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    deadline TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_task_publisher FOREIGN KEY (publisher_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_user_verification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    student_id VARCHAR(64) NOT NULL,
    college VARCHAR(100),
    document_url VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_remark VARCHAR(255),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    CONSTRAINT fk_user_verification_user FOREIGN KEY (user_id) REFERENCES u_user(id),
    CONSTRAINT fk_user_verification_reviewer FOREIGN KEY (reviewer_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_task_favorite (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_favorite_task FOREIGN KEY (task_id) REFERENCES t_task(id),
    CONSTRAINT fk_task_favorite_user FOREIGN KEY (user_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_order (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    poster_id BIGINT NOT NULL,
    helper_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_order_task FOREIGN KEY (task_id) REFERENCES t_task(id),
    CONSTRAINT fk_order_poster FOREIGN KEY (poster_id) REFERENCES u_user(id),
    CONSTRAINT fk_order_helper FOREIGN KEY (helper_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_review (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    score INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES t_order(id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES u_user(id),
    CONSTRAINT fk_review_target FOREIGN KEY (target_user_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_message (
    id BIGSERIAL PRIMARY KEY,
    receiver_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(128),
    content TEXT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_chat_message (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_message_order FOREIGN KEY (order_id) REFERENCES t_order(id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES u_user(id),
    CONSTRAINT fk_chat_message_receiver FOREIGN KEY (receiver_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_notice (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(128),
    content TEXT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_post (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    category VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT,
    media_urls TEXT,
    view_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,
    favorite_count INTEGER NOT NULL DEFAULT 0,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_comment (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    reply_to_comment_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES t_post(id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES u_user(id),
    CONSTRAINT fk_comment_reply FOREIGN KEY (reply_to_comment_id) REFERENCES t_comment(id)
);

CREATE TABLE IF NOT EXISTS t_post_like (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES t_post(id),
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES u_user(id)
);

CREATE TABLE IF NOT EXISTS t_post_favorite (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_favorite_post FOREIGN KEY (post_id) REFERENCES t_post(id),
    CONSTRAINT fk_post_favorite_user FOREIGN KEY (user_id) REFERENCES u_user(id)
);

CREATE INDEX IF NOT EXISTS idx_u_user_username ON u_user(username);
CREATE INDEX IF NOT EXISTS idx_u_user_phone ON u_user(phone);
CREATE INDEX IF NOT EXISTS idx_t_user_verification_user_id ON t_user_verification(user_id);
CREATE INDEX IF NOT EXISTS idx_t_user_verification_status ON t_user_verification(status);
CREATE INDEX IF NOT EXISTS idx_t_task_status ON t_task(status);
CREATE INDEX IF NOT EXISTS idx_t_task_category ON t_task(category);
CREATE INDEX IF NOT EXISTS idx_t_task_favorite_task_id ON t_task_favorite(task_id);
CREATE INDEX IF NOT EXISTS idx_t_task_favorite_user_id ON t_task_favorite(user_id);
CREATE INDEX IF NOT EXISTS idx_t_order_task_id ON t_order(task_id);
CREATE INDEX IF NOT EXISTS idx_t_order_helper_id ON t_order(helper_id);
CREATE INDEX IF NOT EXISTS idx_t_order_status ON t_order(status);
CREATE INDEX IF NOT EXISTS idx_t_message_receiver_id ON t_message(receiver_id);
CREATE INDEX IF NOT EXISTS idx_t_message_read ON t_message(read);
CREATE INDEX IF NOT EXISTS idx_t_chat_message_order_id ON t_chat_message(order_id);
CREATE INDEX IF NOT EXISTS idx_t_chat_message_sender_id ON t_chat_message(sender_id);
CREATE INDEX IF NOT EXISTS idx_t_chat_message_receiver_id ON t_chat_message(receiver_id);
CREATE INDEX IF NOT EXISTS idx_t_notice_user_id ON t_notice(user_id);
CREATE INDEX IF NOT EXISTS idx_t_notice_read ON t_notice(read);
CREATE INDEX IF NOT EXISTS idx_t_post_author_id ON t_post(author_id);
CREATE INDEX IF NOT EXISTS idx_t_post_category ON t_post(category);
CREATE INDEX IF NOT EXISTS idx_t_post_recommended ON t_post(is_recommended);
CREATE INDEX IF NOT EXISTS idx_t_comment_post_id ON t_comment(post_id);
CREATE INDEX IF NOT EXISTS idx_t_post_like_post_id ON t_post_like(post_id);
CREATE INDEX IF NOT EXISTS idx_t_post_like_user_id ON t_post_like(user_id);
CREATE INDEX IF NOT EXISTS idx_t_post_favorite_post_id ON t_post_favorite(post_id);
CREATE INDEX IF NOT EXISTS idx_t_post_favorite_user_id ON t_post_favorite(user_id);