package com.ssau.chat.controller;

import com.ssau.chat.dto.Message.MessageCreateRequest;
import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.Message.MessageUpdateRequest;
import com.ssau.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Сообщения", description = "API для работы с сообщениями чатов")
public class MessageController {
    private final MessageService messageService;

    @Operation(summary = "Отправка сообщения", description = "Отправляет новое сообщение в чат")
    @PostMapping()
    public MessageDTO sendMessage(@RequestBody MessageCreateRequest messageCreateRequest) {
        // TODO сделать websocket
        return messageService.sendMessage(messageCreateRequest);
    }

    @Operation(summary = "Обновление сообщения", description = "Обновляет сообщение в чате")
    @PutMapping("/chat/{chatId}/msg/{msgId}")
    public MessageDTO updateMessage(
            @PathVariable Long chatId, @PathVariable Long msgId,
            @RequestBody MessageUpdateRequest messageUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug(messageUpdateRequest.toString());
        return messageService.updateMessage(chatId, msgId, messageUpdateRequest, userDetails);
    }

    @Operation(summary = "Получение сообщений чата", description = "Возвращает все сообщения для указанного чата")
    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getMessagesByChat(@PathVariable Long chatId) {
        return messageService.getMessagesByChat(chatId);
    }

    @Operation(summary = "Удаление сообщения", description = "Удаляет сообщение из чата")
    @DeleteMapping("/chat/{chatId}/msg/{msgId}")
    public void deleteMessage(@PathVariable Long chatId, @PathVariable Long msgId) {
        messageService.deleteMessage(chatId, msgId);
    }
}