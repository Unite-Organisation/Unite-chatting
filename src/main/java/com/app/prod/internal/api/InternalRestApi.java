package com.app.prod.internal.api;

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

    @PostMapping("/user")
    public void syncUser(@RequestBody UserDto userDto){
        log.info("Syncing user: {}", userDto.username());
        userSyncService.syncUser(userDto);
    }

}
