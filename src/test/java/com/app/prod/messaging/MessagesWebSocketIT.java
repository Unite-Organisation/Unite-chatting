package com.app.prod.messaging;

import com.app.prod.builders.ActivityPersistanceFactory;
import com.app.prod.builders.ConversationMemberPersistanceFactory;
import com.app.prod.builders.ConversationPersistanceFactory;
import com.app.prod.builders.UserPersistanceFactory;
import com.app.prod.config.ApiTest;
import com.app.prod.config.IntegrationTest;
import com.app.prod.messaging.dto.CreateMessageRequest;
import com.app.prod.messaging.dto.MessageResponse;
import com.app.prod.mocking.ApiTestClient;
import com.app.prod.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.sources.tables.records.AppUserRecord;
import org.jooq.sources.tables.records.ConversationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.app.prod.config.Constants.BCRYPT_PASSWORD_ENCODER_STRENGTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
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
    @Autowired
    private ApiTestClient api;

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

        conversationMemberPersistanceFactory.getNewMember().bind(user1.getId(), conversation.getId()).buildAndSave();
        conversationMemberPersistanceFactory.getNewMember().bind(user2.getId(), conversation.getId()).buildAndSave();

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
        // user1 subscribes topic
        SessionHolder sessionHolder = userSubscribesTopic("/topic/conversation/" + conversation.getId(), user1token);

        // user2 sends message
        CreateMessageRequest request = new CreateMessageRequest(conversation.getId(), "Cześć z testu!");
        sessionHolder.session.send("/app/message", request);

        MessageResponse response = sessionHolder.receiverArrival.get(10, TimeUnit.SECONDS);

        assertNotNull(response);
        assertEquals("Cześć z testu!", response.content());
    }

    @Test
    void shouldUserReceiveMessageFromAnotherUser() throws Exception {

        // user2 subscribes topic
        SessionHolder sessionHolder = userSubscribesTopic("/topic/conversation/" + conversation.getId(), user2token);

        // user1 sends message
        StompHeaders headers1 = new StompHeaders();
        headers1.add("Authorization", "Bearer " + user1token);

        StompSession session1 = stompClient
                .connectAsync(wsUrl, new WebSocketHttpHeaders(), headers1, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        String messageText = "Siema User 2, co tam?";
        CreateMessageRequest request = new CreateMessageRequest(conversation.getId(), messageText);
        session1.send("/app/message", request);

        MessageResponse receivedBySecondUser = sessionHolder.receiverArrival.get(10, TimeUnit.SECONDS);

        assertNotNull(receivedBySecondUser);
        assertEquals(messageText, receivedBySecondUser.content());
        assertEquals(user1.getId(), receivedBySecondUser.authorId());
    }

    @Test
    void shoudUserReceiveFileMessage() throws Exception {

        // user2 subscribes topic
        SessionHolder sessionHolder = userSubscribesTopic("/topic/conversation/" + conversation.getId(), user2token);

        MockMultipartFile file = new MockMultipartFile("file", "test-image.png", MediaType.IMAGE_PNG_VALUE, "test content".getBytes());
        mockMvc.perform(multipart("/message/conversation/{id}/file", conversation.getId())
                        .file(file)
                        .header("Authorization", "Bearer " + user1token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());


        MessageResponse receivedBySecondUser = sessionHolder.receiverArrival.get(10, TimeUnit.SECONDS);
        assertNotNull(receivedBySecondUser);
        assertEquals("test-filepath-tokenized", receivedBySecondUser.content());
        assertEquals(user1.getId(), receivedBySecondUser.authorId());
    }

    private SessionHolder userSubscribesTopic(String topic, String token) throws Exception {
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + token);

        CompletableFuture<MessageResponse> receiverArrival = new CompletableFuture<>();

        StompSession session = stompClient
                .connectAsync(wsUrl, new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers2) {
                return MessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers2, Object payload) {
                receiverArrival.complete((MessageResponse) payload);
            }
        });

        return new SessionHolder(session, receiverArrival);
    }

    private record SessionHolder(
            StompSession session,
            CompletableFuture<MessageResponse> receiverArrival
    ) {}

}
