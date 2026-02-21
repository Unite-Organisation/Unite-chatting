package com.app.prod.config.websocket;

import com.app.prod.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketUserManager {

    private final UserService userService;

    public AppUserRecord getCurrentUser(UsernamePasswordAuthenticationToken user){
        UserDetails userDetails = (UserDetails) user.getPrincipal();
        return userService.findByUsername(userDetails.getUsername());
    }
}
