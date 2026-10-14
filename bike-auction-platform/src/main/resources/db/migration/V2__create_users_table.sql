
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       display_name VARCHAR(80) NOT NULL,
                       email VARCHAR(254) NOT NULL,
                       password_hash VARCHAR(60) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_created_at ON users (created_at);
