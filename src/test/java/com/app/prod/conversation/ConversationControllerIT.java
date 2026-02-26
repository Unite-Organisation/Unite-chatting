package com.app.prod.conversation;

import com.app.prod.builders.ConversationPersistanceFactory;
import com.app.prod.builders.UserPersistanceFactory;
import com.app.prod.config.ApiTest;
import com.app.prod.config.IntegrationTest;
import com.app.prod.conversation.dto.ConversationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jooq.sources.tables.records.AppUserRecord;
import org.jooq.sources.tables.records.ConversationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
public class ConversationControllerIT extends IntegrationTest {

    private AppUserRecord user1;
    private AppUserRecord user2;
    private AppUserRecord user3;
    private AppUserRecord loggedUser;

    @Autowired
    private UserPersistanceFactory userPersistanceFactory;
    @Autowired
    private ConversationPersistanceFactory conversationPersistanceFactory;


    @BeforeEach
    void setup() {
        user1 = userPersistanceFactory.getNewUser().withRandomValues().buildAndSave();
        user2 = userPersistanceFactory.getNewUser().withRandomValues().buildAndSave();
        user3 = userPersistanceFactory.getNewUser().withRandomValues().buildAndSave();
        loggedUser = loggedUser();
    }

    @Test
    void getConversations() throws Exception {

        ConversationRecord conversationGroup = conversationPersistanceFactory.getNewConversation()
                .withRandomValues()
                .addUser(user1)
                .addUser(user2)
                .addUser(loggedUser)
                .isGroup(true)
                .buildAndSave();

        ConversationRecord conversationU1U2 = conversationPersistanceFactory.getNewConversation()
                .withRandomValues()
                .addUser(user1)
                .addUser(user2)
                .isGroup(false)
                .buildAndSave();

        ConversationRecord conversationLoggedUserU1 = conversationPersistanceFactory.getNewConversation()
                .withRandomValues()
                .addUser(user1)
                .addUser(user3)
                .isGroup(false)
                .buildAndSave();

        List<ConversationResponse> response1 = readResponse(
                api.performAuthenticated(get("/conversation")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()),
                new TypeReference<>() {}
        );

        assertThat(response1).hasSize(1);
        assertThat(response1.getFirst().id()).isEqualTo(conversationGroup.getId());
        assertThat(response1.getFirst().isGroup()).isTrue();

        List<ConversationResponse> response2 = readResponse(
                api.login(user1).performAuthenticated(get("/conversation")
                                .param("page", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()),
                new TypeReference<>() {}
        );

        assertThat(response2).hasSize(3);


    }

}
