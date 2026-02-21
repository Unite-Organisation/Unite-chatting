package com.app.prod.builders;

import lombok.RequiredArgsConstructor;
import org.jooq.sources.tables.records.AppUserRecord;
import org.jooq.sources.tables.records.ConversationRecord;
import org.jooq.sources.tables.records.ConversationMemberRecord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.app.prod.conversation.repository.ConversationMemberRepository;

@Service
@RequiredArgsConstructor
public class ConversationMemberPersistanceFactory {

    private final Clock clock;
    private final ConversationMemberRepository conversationMemberRepository;

    public Builder getNewMember() {
        return new Builder();
    }

    public class Builder {

        private final ConversationMemberRecord instance;

        public Builder() {
            instance = new ConversationMemberRecord();
        }

        public Builder id(UUID id) {
            instance.setId(id);
            return this;
        }

        public Builder userId(UUID userId) {
            instance.setUserId(userId);
            return this;
        }

        public Builder user(AppUserRecord user) {
            instance.setUserId(user.getId());
            return this;
        }

        public Builder conversationId(UUID conversationId) {
            instance.setConversationId(conversationId);
            return this;
        }

        public Builder conversation(ConversationRecord conversation) {
            instance.setConversationId(conversation.getId());
            return this;
        }

        public Builder joinDate(LocalDateTime joinDate) {
            instance.setJoinDate(joinDate);
            return this;
        }

        public Builder withDefaultValues() {
            instance.setId(UUID.randomUUID());
            instance.setJoinDate(LocalDateTime.now(clock));
            return this;
        }

        public Builder bind(UUID userId, UUID conversationId) {
            instance.setUserId(userId);
            instance.setConversationId(conversationId);
            return this;
        }

        public ConversationMemberRecord build() {
            return instance;
        }

        public ConversationMemberRecord buildAndSave() {
            ConversationMemberRecord record = build();
            conversationMemberRepository.insertOne(record);
            return record;
        }
    }

    public void batchBuildAndSave(List<ConversationMemberRecord> members) {
        conversationMemberRepository.insertMany(members);
    }
}