package com.ssau.chat.controller;

import com.ssau.chat.dto.*;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
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
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatDTO createChat(
            @RequestBody @Valid CreateChatRequest createChatRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug(createChatRequest.toString());
        return chatService.createChatWithUsers(createChatRequest, userDetails);
    }

    @PutMapping("/{id}")
    public ChatDTO updateChat(
            @PathVariable Long id,
            @RequestBody @Valid UpdateChatRequest updateChatRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Updating chat with id: {}", id);
        return chatService.updateChat(id, updateChatRequest, userDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable Long id) {
        chatService.deleteChat(id);
    }

    @Operation(summary = "Получить информацию о чате")
    @GetMapping("/{id}")
    public ChatDTO getChatById(@PathVariable Long id) {
        return chatService.getChatById(id);
    }

    @Operation(summary = "Получить список чатов", description = "Возвращает все чаты текущего пользователя")
    @GetMapping("/all")
    public List<ChatDTO> getAllChats() {
        return chatService.getAllChats();
    }

    @GetMapping
    public List<ChatDTO> getAllChatsByUser(
            @AuthenticationPrincipal UserEntity user) {
        return chatService.getAllChatsByUser(user);
    }

    @GetMapping("/{id}/users")
    public List<UserDTO> getUsersByChatId(@PathVariable Long id) {
        return chatService.getAllUsersByChatId(id);
    }

    @PostMapping("/{chatId}/join/{userId}")
    public ChatUserJoinResponse joinUserInChat(@PathVariable Long chatId, @PathVariable Long userId) {
        return chatService.addUserToChat(chatId, userId);
    }

    @DeleteMapping("/{chatId}/leave/{userId}")
    public void leaveUserFromChat(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.leaveUserFromChat(chatId, userId);
    }
}
