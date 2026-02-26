package com.app.prod.builders;

import com.app.prod.conversation.repository.ConversationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.sources.tables.records.AppUserRecord;
import org.jooq.sources.tables.records.ConversationMemberRecord;
import org.jooq.sources.tables.records.ConversationRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.app.prod.conversation.repository.ConversationRepository;

@Service
@RequiredArgsConstructor
public class ConversationPersistanceFactory {

    private final Clock clock;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationMemberPersistanceFactory conversationMemberPersistanceFactory;

    private List<ConversationMemberRecord> conversationMemberRecords;

    public Builder getNewConversation() {
        conversationMemberRecords = new ArrayList<>();
        return new Builder();
    }

    public class Builder {

        private final ConversationRecord instance;

        public Builder() {
            instance = new ConversationRecord();
        }

        public Builder id(UUID id) {
            instance.setId(id);
            return this;
        }

        public Builder isGroup(Boolean isGroup) {
            instance.setIsGroup(isGroup);
            return this;
        }

        public Builder name(String name) {
            instance.setName(name);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            instance.setCreatedAt(createdAt);
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            instance.setUpdatedAt(updatedAt);
            return this;
        }

        public Builder withRandomValues() {
            instance.setId(UUID.randomUUID());
            instance.setIsGroup(false);
            instance.setName("Chat-" + UUID.randomUUID().toString().substring(0, 8));
            instance.setCreatedAt(LocalDateTime.now(clock));
            instance.setUpdatedAt(LocalDateTime.now(clock));
            return this;
        }

        public Builder addUser(AppUserRecord user) {
            if (instance.getId() == null) {
                instance.setId(UUID.randomUUID());
            }

            ConversationMemberRecord member = conversationMemberPersistanceFactory.getNewMember()
                    .bind(user.getId(), instance.getId())
                    .build();

            conversationMemberRecords.add(member);

            return this;
        }

        public Builder asGroup(String groupName) {
            instance.setIsGroup(true);
            instance.setName(groupName);
            return this;
        }

        public ConversationRecord build() {
            return instance;
        }

        public ConversationRecord buildAndSave() {
            ConversationRecord record = build();
            conversationRepository.insertOne(record);

            if (conversationMemberRecords != null && !conversationMemberRecords.isEmpty()) {
                conversationMemberRepository.insertMany(conversationMemberRecords);
            }

            return record;
        }
    }

    public void batchBuildAndSave(List<ConversationRecord> conversations) {
        conversationRepository.insertMany(conversations);
    }
}