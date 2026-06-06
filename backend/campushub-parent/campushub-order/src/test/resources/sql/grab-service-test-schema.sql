CREATE EXTENSION IF NOT EXISTS postgis;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_task;
DROP TABLE IF EXISTS u_user;

CREATE TABLE u_user (
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

CREATE TABLE t_task (
    id BIGSERIAL PRIMARY KEY,
    publisher_id BIGINT NOT NULL REFERENCES u_user(id),
    title VARCHAR(128) NOT NULL,
    description TEXT,
    category SMALLINT NOT NULL,
    location VARCHAR(128),
    location_point GEOGRAPHY(Point, 4326),
    reward NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    status SMALLINT NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_task_category CHECK (category IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_task_reward CHECK (reward >= 0),
    CONSTRAINT ck_task_status CHECK (status IN (0, 1, 2, 3, 4))
);

CREATE TABLE t_order (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES t_task(id),
    poster_id BIGINT NOT NULL REFERENCES u_user(id),
    helper_id BIGINT NOT NULL REFERENCES u_user(id),
    status SMALLINT NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_order_task UNIQUE (task_id),
    CONSTRAINT ck_order_status CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_order_version CHECK (version >= 0),
    CONSTRAINT ck_order_different_users CHECK (poster_id <> helper_id)
);
