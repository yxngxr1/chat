package com.ssau.chat.controller;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.dto.ChatUserJoinResponse;
import com.ssau.chat.dto.CreateChatRequest;
import com.ssau.chat.dto.UserDTO;
import com.ssau.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatDTO createChat(@RequestBody @Valid CreateChatRequest createChatRequest) {
        return chatService.createChatWithUsers(createChatRequest);
    }

    @DeleteMapping("/{id}")
    public String deleteChat(@PathVariable Long id) {
        chatService.deleteChat(id);
        return "Chat deleted";
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

    @GetMapping("/{id}/users")
    public List<UserDTO> getUsersByChatId(@PathVariable Long id) {
        return chatService.getAllUsersByChatId(id);
    }

    @PostMapping("/{chatId}/join/{userId}")
    public ChatUserJoinResponse joinUserInChat(@PathVariable Long chatId, @PathVariable Long userId) {
        return chatService.addUserToChat(chatId, userId);
    }

    @DeleteMapping("/{chatId}/leave/{userId}")
    public String leaveUserFromChat(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.leaveUserFromChat(chatId, userId);
        return "User leave from chat successfully";
    }


}
