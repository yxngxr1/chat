package com.ssau.chat.controller;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatDTO> createChat(@RequestParam String name) {
        ChatDTO chatDTO = chatService.createChat(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatDTO> getChatById(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getChatById(id));
    }

    @Operation(summary = "Получить список чатов", description = "Возвращает все чаты текущего пользователя")
    @GetMapping
    public ResponseEntity<List<ChatDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @PostMapping("/{chatId}/add-user/{userId}")
    public ResponseEntity<String> addUserToChat(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.addUserToChat(chatId, userId);
        return ResponseEntity.ok("User added to chat successfully");
    }
}
