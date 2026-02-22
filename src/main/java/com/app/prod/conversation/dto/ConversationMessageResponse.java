package com.app.prod.conversation.dto;

import com.app.prod.messaging.repository.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationMessageResponse(
        UUID author,
        String authorName,
        String authorLastName,
        LocalDateTime sendAt,
        String content,
        MessageType messageType
) {
}
