package com.app.prod.messaging;

import com.app.prod.builders.ActivityPersistanceFactory;
import com.app.prod.builders.ConversationMemberPersistanceFactory;
import com.app.prod.builders.ConversationPersistanceFactory;
import com.app.prod.builders.UserPersistanceFactory;
import com.app.prod.config.IntegrationTest;
import com.app.prod.messaging.dto.CreateMessageRequest;
import com.app.prod.messaging.dto.MessageResponse;
import com.app.prod.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.sources.tables.records.AppUserRecord;
import org.jooq.sources.tables.records.ConversationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.app.prod.config.Constants.BCRYPT_PASSWORD_ENCODER_STRENGTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessagesWebSocketIT extends IntegrationTest {

    private WebSocketStompClient stompClient;
    private String wsUrl;

    private AppUserRecord user1;
    private String user1token;
    private AppUserRecord user2;
    private String user2token;
    private ConversationRecord conversation;

    @LocalServerPort
    private int port;
    @Autowired
    private UserPersistanceFactory userPersistanceFactory;
    @Autowired
    private ConversationPersistanceFactory conversationPersistanceFactory;
    @Autowired
    private ConversationMemberPersistanceFactory conversationMemberPersistanceFactory;
    @Autowired
    private ActivityPersistanceFactory activityPersistanceFactory;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_PASSWORD_ENCODER_STRENGTH);
        String password = "testowehaslo";

        user1 = userPersistanceFactory.getNewUser()
                .withRandomValues()
                .password(passwordEncoder.encode("testowehaslo"))
                .buildAndSave();

        user2 = userPersistanceFactory.getNewUser()
                .withRandomValues()
                .password(passwordEncoder.encode("testowehaslo"))
                .buildAndSave();

        activityPersistanceFactory.getNewActivity().withRandomValues().userId(user1.getId()).buildAndSave();
        activityPersistanceFactory.getNewActivity().withRandomValues().userId(user2.getId()).buildAndSave();

        conversation = conversationPersistanceFactory.getNewConversation()
                .withRandomValues()
                .buildAndSave();

        conversationMemberPersistanceFactory.getNewMember().bind(user1.getId(), conversation.getId());
        conversationMemberPersistanceFactory.getNewMember().bind(user2.getId(), conversation.getId());

        prepareClient(password);
    }

    private void prepareClient(String password) {
        user1token = userService.generateToken(user1.getUsername(), password);
        user2token = userService.generateToken(user2.getUsername(), password);

        wsUrl = "ws://localhost:" + port + "/v1/api/ws";
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @Test
    void shouldSendMessageAndReceiveResponse() throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + user1token);

        CompletableFuture<MessageResponse> resultKeeper = new CompletableFuture<>();

        StompSession session = stompClient
                .connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // SUBSCRIBE
        session.subscribe("/topic/conversation/" + conversation.getId(), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                resultKeeper.complete((MessageResponse) payload);
            }
        });

        // SEND
        CreateMessageRequest request = new CreateMessageRequest(conversation.getId(), "Cześć z testu!");
        session.send("/app/message", request);

        MessageResponse response = resultKeeper.get(10, TimeUnit.SECONDS);

        assertNotNull(response);
        assertEquals("Cześć z testu!", response.content());
    }

    @Test
    void shouldUserReceiveMessageFromAnotherUser() throws Exception {

        // RECEIVER SUBSCRIBES
        StompHeaders headers2 = new StompHeaders();
        headers2.add("Authorization", "Bearer " + user2token);

        CompletableFuture<MessageResponse> receiverArrival = new CompletableFuture<>();

        StompSession session = stompClient
                .connectAsync(wsUrl, new WebSocketHttpHeaders(), headers2, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/conversation/" + conversation.getId(), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receiverArrival.complete((MessageResponse) payload);
            }
        });

        // SENDER SENDS
        StompHeaders headers1 = new StompHeaders();
        headers1.add("Authorization", "Bearer " + user1token);

        StompSession session1 = stompClient
                .connectAsync(wsUrl, new WebSocketHttpHeaders(), headers1, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        String messageText = "Siema User 2, co tam?";
        CreateMessageRequest request = new CreateMessageRequest(conversation.getId(), messageText);
        session1.send("/app/message", request);

        MessageResponse receivedBySecondUser = receiverArrival.get(10, TimeUnit.SECONDS);

        assertNotNull(receivedBySecondUser);
        assertEquals(messageText, receivedBySecondUser.content());
        assertEquals(user1.getId(), receivedBySecondUser.authorId());
    }

}
