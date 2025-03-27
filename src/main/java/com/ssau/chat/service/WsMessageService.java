package com.ssau.chat.service;

import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.Message.WsMessageCreateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.entity.UserEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.validation.Validator;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsMessageService {
    private final Validator validator;
    private final ChatUserService chatUserService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public void processWebSocketMessage(WsMessageCreateRequest request, Principal principal) {
        log.info("Processing WebSocket message: {}", request);

        // 1. Валидация входящего сообщения
        Set<ConstraintViolation<WsMessageCreateRequest>> violations = validator.validate(
                request
        );

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Long userId = getCurrentUserId(principal);
        if (userId == null) {
            throw new AccessDeniedException("Не удалось определить пользователя");
        }

        // 2. Проверка, находится ли пользователь в чате
        if (!chatUserService.userInChat(request.getChatId(), userId)) {
            throw new AccessDeniedException("Тебя нет в этом чате");
        }

        MessageDTO messageDTO = messageService.createMessage(
                request.getChatId(),
                userId,
                request.getContent()
        );

        List<UserDTO> usersInChat = chatUserService.findAllUsersByChatId(request.getChatId());

        usersInChat.forEach(user -> {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(user.getId()), "/queue/messages", messageDTO
            );
        });
    }

    private Long getCurrentUserId(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object userDetails = authentication.getPrincipal();
            if (userDetails instanceof UserEntity userEntity) {
                return userEntity.getId();
            }
        }
        return null;
    }
}