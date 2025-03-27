package com.ssau.chat.controller;

import com.ssau.chat.dto.Message.WsMessageCreateRequest;
import com.ssau.chat.service.WsMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WsController {

    private final WsMessageService wsMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload WsMessageCreateRequest wsMessageCreateRequest, Principal principal) {
        wsMessageService.processWebSocketMessage(wsMessageCreateRequest, principal);
    }
}
