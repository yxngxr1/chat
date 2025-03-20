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
    @PutMapping("/{id}")
    public ChatDTO updateChat(
            @PathVariable Long id,
            @RequestBody @Valid ChatUpdateRequest chatUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Updating chat with id: {}", id);
        return chatService.updateChat(id, chatUpdateRequest, userDetails);
    }

    @Operation(summary = "Удаление чата", description = "Удаляет чат по идентификатору")
    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable Long id) {
        chatService.deleteChat(id);
    }

    @Operation(summary = "Получить информацию о чате", description = "Возвращает информацию о чате по его идентификатору")
    @GetMapping("/{id}")
    public ChatDTO getChatById(@PathVariable Long id) {
        return chatService.getChatById(id);
    }

    @Operation(summary = "Получить список чатов", description = "Возвращает все чаты текущего пользователя")
    @GetMapping("/all")
    public List<ChatDTO> getAllChats() {
        return chatService.getAllChats();
    }

    @Operation(summary = "Получить чаты текущего пользователя", description = "Возвращает чаты, в которых участвует текущий пользователь")
    @GetMapping
    public List<ChatDTO> getAllChatsByUser(
            @AuthenticationPrincipal UserEntity user) {
        return chatService.getAllChatsByUser(user);
    }

    @Operation(summary = "Получить список пользователей чата", description = "Возвращает список пользователей, которые состоят в чате")
    @GetMapping("/{id}/users")
    public List<UserDTO> getUsersByChatId(@PathVariable Long id) {
        return chatService.getAllUsersByChatId(id);
    }

    @Operation(summary = "Добавление пользователя в чат", description = "Добавляет пользователя в чат")
    @PostMapping("/{chatId}/join/{userId}")
    public ChatUserCreateResponse joinUserInChat(@PathVariable Long chatId, @PathVariable Long userId) {
        return chatService.addUserToChat(chatId, userId);
    }

    @Operation(summary = "Удаление пользователя из чата", description = "Удаляет пользователя из чата")
    @DeleteMapping("/{chatId}/leave/{userId}")
    public void leaveUserFromChat(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.leaveUserFromChat(chatId, userId);
    }
}
