package com.app.prod.messaging.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse (
    UUID id,
    UUID conversationId,
    UUID authorId,
    String authorDisplayName,
    String content,
    LocalDateTime sentAt
) {
}
