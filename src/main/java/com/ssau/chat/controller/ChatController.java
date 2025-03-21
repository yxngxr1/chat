package com.ssau.chat.controller;

import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.Chat.ChatCreateRequest;
import com.ssau.chat.dto.Chat.ChatUpdateRequest;
import com.ssau.chat.dto.ChatUser.ChatUserCreateResponse;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "Чаты", description = "API для управления чатами")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Создание нового чата", description = "Создает новый чат с участниками")
    @PostMapping
    public ChatDTO createChat(
            @RequestBody @Valid ChatCreateRequest chatCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug(chatCreateRequest.toString());
        return chatService.createChatWithUsers(chatCreateRequest, userDetails);
    }

    @Operation(summary = "Обновление чата", description = "Обновляет информацию о чате")
    @PutMapping("/{chatId}")
    public ChatDTO updateChat(
            @PathVariable Long chatId,
            @RequestBody @Valid ChatUpdateRequest chatUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Updating chat with id: {}", chatId);
        return chatService.updateChat(chatId, chatUpdateRequest, userDetails);
    }

    @Operation(summary = "Удаление чата", description = "Удаляет чат по идентификатору")
    @DeleteMapping("/{chatId}")
    public void deleteChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.deleteChat(chatId, userDetails);
    }

    @Operation(summary = "Получить информацию о чате", description = "Возвращает информацию о чате по его идентификатору")
    @GetMapping("/{chatId}")
    public ChatDTO getChatById(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return chatService.getChatById(chatId, userDetails);
    }

    @Operation(summary = "Получить список чатов", description = "Возвращает все чаты текущего пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<ChatDTO> getAllChats() {
        return chatService.getAllChats();
    }

    @Operation(summary = "Получить чаты текущего пользователя", description = "Возвращает чаты, в которых участвует текущий пользователь")
    @GetMapping
    public List<ChatDTO> getAllChatsByUser(
            @AuthenticationPrincipal UserEntity userDetails) {
        return chatService.getAllChatsByUser(userDetails);
    }

    @Operation(summary = "Получить список пользователей чата", description = "Возвращает список пользователей, которые состоят в чате")
    @GetMapping("/{chatId}/users")
    public List<UserDTO> getUsersByChatId(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserEntity userDetails) {
        return chatService.getAllUsersByChatId(chatId, userDetails);
    }

    @Operation(summary = "Добавление пользователя в чат", description = "Добавляет пользователя в чат")
    @PostMapping("/{chatId}/join/{userId}")
    public ChatUserCreateResponse joinUserInChat(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserEntity userDetails) {
        return chatService.addUserToChat(chatId, userId, userDetails);
    }

    @Operation(summary = "Удаление пользователя из чата", description = "Удаляет пользователя из чата")
    @DeleteMapping("/{chatId}/leave/{userId}")
    public void leaveUserFromChat(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserEntity userDetails) {
        chatService.leaveUserFromChat(chatId, userId, userDetails);
    }
}
