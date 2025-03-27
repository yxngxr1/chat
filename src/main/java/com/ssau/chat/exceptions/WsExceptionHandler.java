package com.ssau.chat.exceptions;

import com.ssau.chat.dto.Message.WsMessageErrorResponse;
import com.ssau.chat.entity.UserEntity;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WsExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(ConstraintViolationException.class)
    public void handleValidationException(ConstraintViolationException e, Message message) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(message);
        Principal principal = headerAccessor.getUser();

        Long userId = getCurrentUserId(principal);
        if (userId != null) {
//            log.debug(userId.toString());
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId), "/queue/messages",
                    new WsMessageErrorResponse("Ошибка валидации: " + e.getMessage())
            );
        }
    }

    @MessageExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e, Principal principal) {
        log.warn("Access denied for user {}: {}", principal.getName(), e.getMessage());

        Long userId = getCurrentUserId(principal);
        if (userId != null) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId), "/queue/messages",
                    new WsMessageErrorResponse("Ошибка доступа: " + e.getMessage())
            );
        }
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