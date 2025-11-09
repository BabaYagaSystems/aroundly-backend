-- ===========================
-- USERS TABLE
-- ===========================
-- This table stores user information synced from Firebase Authentication
-- The firebase_uid is the link between Firebase and our database

CREATE TABLE IF NOT EXISTS users (
                                     id              BIGINT NOT NULL PRIMARY KEY,
                                     firebase_uid    VARCHAR(128) NOT NULL UNIQUE,
    email           VARCHAR(255),
    display_name    VARCHAR(255),
    picture_url     VARCHAR(512),
    fcm_token       VARCHAR(255),
    range_km        INT DEFAULT 5,
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP
    );

-- Create unique index on firebase_uid for fast lookups
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);

-- Create index on email for lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);