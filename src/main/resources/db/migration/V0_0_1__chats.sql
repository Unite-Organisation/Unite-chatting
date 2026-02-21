CREATE TABLE conversation
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    is_group BOOLEAN NOT NULL,
    name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conversation_member
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES conversation(id) ON DELETE CASCADE,
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE message
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES conversation(id) ON DELETE CASCADE,
    content TEXT,
    send_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages_read
(
    message_id UUID REFERENCES message(id) ON DELETE CASCADE,
    viewed_by UUID REFERENCES app_user(id) ON DELETE CASCADE,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(message_id, viewed_by)
);