CREATE TABLE user_role
(
    id UUID PRIMARY KEY,
    user_role VARCHAR(15) NOT NULL
);

INSERT INTO user_role (id, user_role) VALUES
    ('a3f5c9d2-4b8e-4d61-9a67-12c4e9b7f8a1', 'RESIDENT'),
    ('b9e2a7f4-6c1d-47d2-8e93-45ab2c1d3f27', 'MANAGER'),
    ('c4d8e1a9-9f2b-4c6f-82d5-67d8a1c2e5b3', 'ADMIN');

CREATE TABLE app_user
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(20) NOT NULL,
    last_name  VARCHAR(20) NOT NULL,
    email      VARCHAR(50) UNIQUE,
    username   VARCHAR(20) NOT NULL UNIQUE,
    password   VARCHAR(200) NOT NULL,
    user_role  UUID REFERENCES user_role(id) ON DELETE CASCADE,
    status     VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
