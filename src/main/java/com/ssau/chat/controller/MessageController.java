package com.ssau.chat.controller;

import com.ssau.chat.dto.MessageDTO;
import com.ssau.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageDTO messageDTO) {
        MessageDTO savedMessage = messageService.sendMessage(messageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getMessagesByChat(@PathVariable Long chatId) {
        return messageService.getMessagesByChat(chatId);
    }
}