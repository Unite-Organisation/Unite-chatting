package com.app.prod.internal.api;

import com.app.prod.conversation.service.ConversationService;
import com.app.prod.internal.dtos.ConversationBulkActionDto;
import com.app.prod.internal.dtos.UserDto;
import com.app.prod.user.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalRestApi {

    private final UserSyncService userSyncService;
    private final ConversationService conversationService;

    @PostMapping("/user")
    public void syncUser(@RequestBody UserDto userDto){
        log.info("Syncing user: {}", userDto.username());
        userSyncService.syncUser(userDto);
    }

    @PostMapping("/bulk-conversation")
    public void createConversations(@RequestBody ConversationBulkActionDto dto) {
        log.info("Creating conversations with {} contacts", dto.contacts().size());
        conversationService.createConversationsWithMembers(dto.userId(), dto.contacts());
    }

}
