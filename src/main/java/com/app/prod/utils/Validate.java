package com.app.prod.utils;

import com.app.prod.conversation.repository.ConversationRepository;
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

    public void user(UUID id){
        if(!userRepository.exists(id)){
            throw new EntityNotPresentException(
                    String.format("User with id: %s doesn't exist.", id),
                    AppUser.class.getSimpleName()
            );
        }
    }

    public void thatUsernameIsFree(String username){
        if(userRepository.findByUsername(username).isPresent()){
            throw new DataAlreadyExistsException("This username is already taken.");
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
}
