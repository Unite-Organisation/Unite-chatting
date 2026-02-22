package com.app.prod.messaging.api;

import com.app.prod.config.websocket.WebSocketUserManager;
import com.app.prod.messaging.dto.CreateMessageRequest;
import com.app.prod.messaging.dto.MessageResponse;
import com.app.prod.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageSocketApi {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketUserManager webSocketUserManager;

    @MessageMapping("/message")
    public void sendMessage(CreateMessageRequest request, Principal principal){
        var user = webSocketUserManager.getCurrentUser((UsernamePasswordAuthenticationToken) principal);
        MessageResponse response = messageService.createMessage(request, user);
        messagingTemplate.convertAndSend("/topic/conversation/" + request.conversationId(), response);
    }

}
