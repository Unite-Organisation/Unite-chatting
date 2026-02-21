CREATE TABLE activity
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES app_user(id) ON DELETE CASCADE,
    last_seen  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status     VARCHAR(30) NOT NULL
);
