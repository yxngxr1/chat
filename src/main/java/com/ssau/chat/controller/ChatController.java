package com.ssau.chat.controller;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.dto.ChatUserJoinResponse;
import com.ssau.chat.dto.CreateChatRequest;
import com.ssau.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatDTO createChat(@RequestBody @Valid CreateChatRequest createChatRequest) {
        return chatService.createChatWithUsers(createChatRequest);
    }

    @Operation(summary = "Получить информацию о чате")
    @GetMapping("/{id}")
    public ChatDTO getChatById(@PathVariable Long id) {
        return chatService.getChatById(id);
    }

    @Operation(summary = "Получить список чатов", description = "Возвращает все чаты текущего пользователя")
    @GetMapping
    public List<ChatDTO> getAllChats() {
        return chatService.getAllChats();
    }

    @PostMapping("/{chatId}/join/{userId}")
    public ChatUserJoinResponse joinUserInChat(@PathVariable Long chatId, @PathVariable Long userId) {
        return chatService.addUserToChat(chatId, userId);
    }

//    @PostMapping("/{chatId}/leave/{userId}")
//    public  leaveUserFromChat(@PathVariable Long chatId, @PathVariable Long userId) {
//        chatService.addUserToChat(chatId, userId);
//        return ResponseEntity.ok("User added to chat successfully");
//    }
}
