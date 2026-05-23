-- =====================================================================
-- Turggut Student Management System - Initial Schema
-- =====================================================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    email           VARCHAR(160) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(16)  NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);

CREATE TABLE students (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    student_number  VARCHAR(32)  NOT NULL UNIQUE,
    first_name      VARCHAR(80)  NOT NULL,
    last_name       VARCHAR(80)  NOT NULL,
    national_id     VARCHAR(32)  UNIQUE,
    phone           VARCHAR(32),
    date_of_birth   DATE,
    department      VARCHAR(120),
    enrollment_year INTEGER,
    address         VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_last_first ON students(last_name, first_name);
CREATE INDEX idx_students_department ON students(department);

CREATE TABLE teachers (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_number VARCHAR(32)  NOT NULL UNIQUE,
    first_name      VARCHAR(80)  NOT NULL,
    last_name       VARCHAR(80)  NOT NULL,
    title           VARCHAR(40),
    department      VARCHAR(120),
    phone           VARCHAR(32),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    description     TEXT,
    credits         INTEGER      NOT NULL CHECK (credits BETWEEN 1 AND 12),
    department      VARCHAR(120),
    semester        VARCHAR(20),
    teacher_id      BIGINT REFERENCES teachers(id) ON DELETE SET NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_courses_teacher ON courses(teacher_id);
CREATE INDEX idx_courses_department ON courses(department);

CREATE TABLE enrollments (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    course_id       BIGINT       NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    semester        VARCHAR(20)  NOT NULL,
    midterm_grade   NUMERIC(5,2) CHECK (midterm_grade BETWEEN 0 AND 100),
    final_grade     NUMERIC(5,2) CHECK (final_grade BETWEEN 0 AND 100),
    letter_grade    VARCHAR(3),
    status          VARCHAR(16)  NOT NULL DEFAULT 'ENROLLED' CHECK (status IN ('ENROLLED','COMPLETED','DROPPED','FAILED')),
    enrolled_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, course_id, semester)
);

CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);

CREATE TABLE fees (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    semester        VARCHAR(20)  NOT NULL,
    amount          NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    due_date        DATE         NOT NULL,
    paid_at         TIMESTAMP,
    status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PARTIAL','PAID','OVERDUE','WAIVED')),
    description     VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, semester)
);

CREATE INDEX idx_fees_student ON fees(student_id);
CREATE INDEX idx_fees_status ON fees(status);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    revoked         BOOLEAN      NOT NULL DEFAULT FALSE,
    user_agent      VARCHAR(255),
    ip_address      VARCHAR(64),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    occurred_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_username  VARCHAR(64),
    actor_role      VARCHAR(16),
    action          VARCHAR(64)  NOT NULL,
    entity_type     VARCHAR(64),
    entity_id       VARCHAR(64),
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(255),
    request_id      VARCHAR(64),
    status          VARCHAR(16)  NOT NULL DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS','FAILURE')),
    details         TEXT
);

CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at DESC);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_username);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
