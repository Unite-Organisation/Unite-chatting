package com.app.prod.utils;

import com.app.prod.conversation.repository.ConversationMemberRepository;
import com.app.prod.conversation.repository.ConversationRepository;
import com.app.prod.exceptions.exceptions.BadRequestException;
import com.app.prod.exceptions.exceptions.DataAlreadyExistsException;
import com.app.prod.exceptions.exceptions.EntityNotPresentException;
import com.app.prod.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.sources.tables.AppUser;
import org.jooq.sources.tables.Conversation;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Validate {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public void user(UUID id){
        if(!userRepository.exists(id)){
            throw new EntityNotPresentException(
                    String.format("User with id: %s doesn't exist.", id),
                    AppUser.class.getSimpleName()
            );
        }
    }

    public void conversation(UUID id){
        if(!conversationRepository.exists(id)){
            throw new EntityNotPresentException(
                    String.format("Conversation with id: %s doesn't exist.", id),
                    Conversation.class.getSimpleName()
            );
        }
    }

    public void thatUserBelongsToConversation(UUID userId, UUID conversationId){
        if(!conversationMemberRepository.userBelongToConversation(userId, conversationId)){
            throw new BadRequestException(String.format("User: %s does not belong to conversation: %s", userId, conversationId));
        }
    }
}
