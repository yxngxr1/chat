package com.ssau.chat.controller;

import com.ssau.chat.dto.Message.ChatNotification;
import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.Message.WsMessageCreateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.service.ChatUserService;
import com.ssau.chat.service.MessageService;
import com.ssau.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final ChatUserService chatUserService;
    private final MessageService messageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload WsMessageCreateRequest wsMessageCreateRequest) {
        log.info("processMessage: {}", wsMessageCreateRequest);
        List<UserDTO> userIds = chatUserService.findAllUsersByChatId(wsMessageCreateRequest.getChatId());

        MessageDTO messageDTO = messageService.createMessage(
                wsMessageCreateRequest.getChatId(),
                wsMessageCreateRequest.getSenderId(),
                wsMessageCreateRequest.getContent());

        // Отправляем каждому пользователю сообщение
        userIds.forEach(user -> {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(user.getId()), "/queue/messages", messageDTO
            );
        });

    }
}
