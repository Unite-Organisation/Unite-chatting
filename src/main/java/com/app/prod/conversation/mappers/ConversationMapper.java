package com.app.prod.conversation.mappers;

import com.app.prod.conversation.dto.ConversationRequest;
import com.app.prod.conversation.dto.ConversationResponse;
import org.jooq.sources.tables.records.ConversationRecord;
import org.jooq.sources.tables.records.AppUserRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationMapper {

    public static ConversationRecord fromRequestToRecord(ConversationRequest request, UUID id, LocalDateTime now) {
        return new ConversationRecord(
                id,
                request.isGroup(),
                request.name(),
                now,
                now
        );
    }
}
