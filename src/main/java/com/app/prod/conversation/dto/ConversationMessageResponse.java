package com.app.prod.conversation.dto;

import com.app.prod.messaging.repository.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationMessageResponse(
        UUID id,
        UUID authorId,
        String authorDisplayName,
        String content,
        LocalDateTime sentAt,
        MessageType messageType
) {
}
