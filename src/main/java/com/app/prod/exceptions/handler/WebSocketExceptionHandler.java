package com.app.prod.exceptions.handler;

import com.app.prod.exceptions.exceptions.BadRequestException;
import com.app.prod.exceptions.exceptions.UserNotAuthenticatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(BadRequestException.class)
    @SendToUser("/queue/errors")
    public String handleBadRequest(BadRequestException e) {
        log.error("WebSocket validation error: {}", e.getMessage());
        return e.getMessage();
    }

    @MessageExceptionHandler(UserNotAuthenticatedException.class)
    @SendToUser("/queue/errors")
    public String handleUserNotAuthenticated(UserNotAuthenticatedException e) {
        log.error("WebSocket authentication error: {}", e.getMessage());
        return e.getMessage();
    }

}
