package com.ssau.chat.controller;

import com.ssau.chat.dto.MessageDTO;
import com.ssau.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping()
    public MessageDTO sendMessage(@RequestBody MessageDTO messageDTO) {
        // TODO сделать dto по нормальному и вообще наверное убрать и сделать websocket
        return messageService.sendMessage(messageDTO);
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getMessagesByChat(@PathVariable Long chatId) {
        return messageService.getMessagesByChat(chatId);
    }

    @DeleteMapping("/chat/{chatId}/msg/{msgId}")
    public String deleteMessage(@PathVariable Long chatId, @PathVariable Long msgId) {
        messageService.deleteMessage(chatId, msgId);
        return "Message deleted";
    }
}