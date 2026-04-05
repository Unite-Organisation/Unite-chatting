package com.app.prod.messaging.repository;

import com.app.prod.conversation.dto.ConversationMessageResponse;
import com.app.prod.messaging.dto.MessageResponse;
import com.app.prod.utils.BaseJooqRepository;
import com.app.prod.utils.Pagination;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.sources.tables.Message;
import org.jooq.sources.tables.records.MessageRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.jooq.sources.Tables.APP_USER;
import static org.jooq.sources.Tables.MESSAGE;

@Repository
public class MessageRepository extends BaseJooqRepository<Message, MessageRecord, UUID> {
    protected MessageRepository(DSLContext dsl) {
        super(dsl, Message.MESSAGE, Message.MESSAGE.ID);
    }

    public List<MessageResponse> findByConversationId(UUID conversationId, UUID lastMessageId, int pageSize) {
        var query = dslContext.select(
                        APP_USER.ID,
                        APP_USER.FIRST_NAME,
                        APP_USER.LAST_NAME,
                        MESSAGE.SEND_AT,
                        MESSAGE.CONTENT,
                        MESSAGE.MESSAGE_TYPE,
                        MESSAGE.ID
                )
                .from(MESSAGE)
                .leftJoin(APP_USER).on(APP_USER.ID.eq(MESSAGE.SENDER_ID))
                .where(MESSAGE.CONVERSATION_ID.eq(conversationId));

        if (lastMessageId != null) {
            query = query.and(MESSAGE.SEND_AT.lt(
                    dslContext.select(MESSAGE.SEND_AT)
                            .from(MESSAGE)
                            .where(MESSAGE.ID.eq(lastMessageId))
            ));
        }

        return query
                .orderBy(MESSAGE.SEND_AT.desc())
                .limit(pageSize)
                .fetch(record -> new MessageResponse(
                        record.get(MESSAGE.ID),
                        record.get(APP_USER.ID),
                        record.get(APP_USER.FIRST_NAME) + " " + record.get(APP_USER.LAST_NAME),
                        record.get(MESSAGE.CONTENT),
                        record.get(MESSAGE.SEND_AT),
                        MessageType.valueOf(record.get(MESSAGE.MESSAGE_TYPE))
                ));
    }
}
