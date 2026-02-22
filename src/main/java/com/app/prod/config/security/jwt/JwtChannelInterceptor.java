package com.app.prod.config.security.jwt;

import com.app.prod.activity.enums.ActivityStatus;
import com.app.prod.activity.service.ActivityService;
import com.app.prod.config.security.UserDetailsServiceImpl;
import com.app.prod.config.websocket.WebSocketUserManager;
import com.app.prod.exceptions.exceptions.BadRequestException;
import com.app.prod.exceptions.exceptions.UserNotAuthenticatedException;
import com.app.prod.utils.Validate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final WebSocketUserManager webSocketUserManager;
    private final Validate validate;
    private final ObjectMapper objectMapper;
    private final ActivityService activityService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case DISCONNECT -> handleDisconnectUnsubscribe(accessor);
            case SEND -> handleSend(accessor, message);
        }

        return message;
    }

    // handles first connection establishment by fetching user and storing it in STOMP session
    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            var userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                accessor.setUser(authentication);
                var user = webSocketUserManager.getCurrentUser(authentication);
                activityService.updateActivityStatus(user.getId(), ActivityStatus.ONLINE);
                log.info("WebSocket user authenticated: {}", username);
            }
        }
    }

    // validates that user subscribing to topic have access to conversation
    private void handleSubscribe(StompHeaderAccessor accessor) {
        UUID conversationId = extractIdFromDestination(accessor.getDestination());
        validateAccess(accessor, conversationId);
    }

    // validates that user sending message to topic have access to conversation
    private void handleSend(StompHeaderAccessor accessor, Message<?> message) {
        UUID conversationId = extractIdFromPayload(message);
        validateAccess(accessor, conversationId);
    }

    private UUID extractIdFromDestination(String destination) {
        if (destination != null && destination.startsWith("/topic/conversation/")) {
            try {
                return UUID.fromString(destination.replace("/topic/conversation/", ""));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid conversation ID format in destination");
            }
        }
        return null;
    }

    private UUID extractIdFromPayload(Message<?> message) {
        try {
            byte[] payload = (byte[]) message.getPayload();
            JsonNode node = objectMapper.readTree(payload);
            if (node.has("conversationId")) {
                return UUID.fromString(node.get("conversationId").asText());
            }
        } catch (Exception e) {
            log.error("Failed to parse payload for conversationId", e);
        }
        return null;
    }

    private void validateAccess(StompHeaderAccessor accessor, UUID conversationId) {
        if (conversationId == null) return;

        var authentication = (UsernamePasswordAuthenticationToken) accessor.getUser();
        if (authentication == null) {
            throw new UserNotAuthenticatedException("User not authenticated");
        }

        var user = webSocketUserManager.getCurrentUser(authentication);
        activityService.updateActivityStatus(user.getId(), ActivityStatus.ONLINE);
        validate.thatUserBelongsToConversation(user.getId(), conversationId);
        log.info("Access validated for user {} to conversation {}", user.getUsername(), conversationId);
    }


    private void handleDisconnectUnsubscribe(StompHeaderAccessor accessor) {
        var authentication = (UsernamePasswordAuthenticationToken) accessor.getUser();

        if (authentication == null) {
            throw new UserNotAuthenticatedException("User not authenticated");
        }

        var user = webSocketUserManager.getCurrentUser(authentication);
        activityService.updateActivityStatus(user.getId(), ActivityStatus.OFFLINE);
        log.info("User {} disconnected from WebSocket", user.getUsername());
    }


}
