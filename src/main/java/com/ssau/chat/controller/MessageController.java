package com.ssau.chat.controller;

import com.ssau.chat.dto.Message.*;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.service.ChatService;
import com.ssau.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
@Tag(name = "Сообщения", description = "API для работы с сообщениями чатов")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    // TODO сделать отправку и прием сообщений по websocket

    @Operation(summary = "Отправка сообщения", description = "Отправляет новое сообщение в чат")
    @PostMapping("/chat/{chatId}")
    public MessageDTO sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody MessageCreateRequest messageCreateRequest,
            @AuthenticationPrincipal UserEntity userDetails) {
        return messageService.sendMessage(chatId, messageCreateRequest, userDetails);
    }

    @Operation(summary = "Обновление сообщения", description = "Обновляет сообщение в чате")
    @PutMapping("/chat/{chatId}/msg/{msgId}")
    public MessageDTO updateMessage(
            @PathVariable Long chatId,
            @PathVariable Long msgId,
            @RequestBody MessageUpdateRequest messageUpdateRequest,
            @AuthenticationPrincipal UserEntity userDetails) {
        return messageService.updateMessage(chatId, msgId, messageUpdateRequest, userDetails);
    }

    @Operation(summary = "Получение всех сообщений чата", description = "Возвращает все сообщения для указанного чата")
    @GetMapping("/chat/{chatId}/all")
    public List<MessageDTO> getAllMessagesByChatId(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserEntity userDetails) {
        return messageService.getMessagesByChat(chatId, userDetails);
    }

    @Operation(summary = "Порционная выдача сообщений чата", description = "Возвращает сообщения указанного чата по указанному времени")
    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getMessagesBefore(
            @PathVariable Long chatId,
            @RequestParam(required = false) LocalDateTime localDateTime,
            @RequestParam(defaultValue = "100") int size,
            @AuthenticationPrincipal UserEntity userDetails) {
        if (localDateTime == null) {
            localDateTime = LocalDateTime.now();
//            long unixSeconds = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
//            log.info("Unix timestamp (сек): {}", unixSeconds);
        }
        return messageService.getMessagesBefore(chatId, localDateTime, size, userDetails);
    }

    @Operation(summary = "Удаление сообщения", description = "Удаляет сообщение из чата")
    @DeleteMapping("/chat/{chatId}/msg/{msgId}")
    public void deleteMessage(
            @PathVariable Long chatId,
            @PathVariable Long msgId,
            @AuthenticationPrincipal UserEntity userDetails) {
        messageService.deleteMessage(chatId, msgId, userDetails);
    }
}