package com.app.prod.messaging.api;

import com.app.prod.config.security.GlobalSecurityManager;
import com.app.prod.messaging.dto.CreateMessageRequest;
import com.app.prod.messaging.dto.MessageResponse;
import com.app.prod.messaging.service.MessageService;
import com.app.prod.storage.AbstractStorage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("message")
@RequiredArgsConstructor
@Tag(name = "Messages")
public class MessageRestApi {

    private final MessageService messageService;
    private final GlobalSecurityManager globalSecurityManager;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping()
    @Deprecated(since = "Dont use this endpoint. Use websocket instead.")
    public void createMessage(@RequestBody CreateMessageRequest request){
        var user = globalSecurityManager.getCurrentUser();
        messageService.createMessage(request, user);
    }

    @PostMapping("/conversation/{id}/file")
    public void uploadFile(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        var user = globalSecurityManager.getCurrentUser();
        MessageResponse response = messageService.createFileMessage(id, file, user);
        messagingTemplate.convertAndSend("/topic/conversation/" + id, response);
    }

}
