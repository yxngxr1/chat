package com.ssau.chat.controller;

import com.ssau.chat.dto.Message.MessageCreateRequest;
import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.Message.MessageUpdateRequest;
import com.ssau.chat.service.MessageService;
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
public class MessageController {
    private final MessageService messageService;

    @PostMapping()
    public MessageDTO sendMessage(@RequestBody MessageCreateRequest messageCreateRequest) {
        // TODO сделать websocket
        return messageService.sendMessage(messageCreateRequest);
    }

    @PutMapping("/chat/{chatId}/msg/{msgId}")
    public MessageDTO updateMessage(
            @PathVariable Long chatId, @PathVariable Long msgId,
            @RequestBody MessageUpdateRequest messageUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug(messageUpdateRequest.toString());
        return messageService.updateMessage(chatId, msgId, messageUpdateRequest, userDetails);
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getMessagesByChat(@PathVariable Long chatId) {
        return messageService.getMessagesByChat(chatId);
    }

    @DeleteMapping("/chat/{chatId}/msg/{msgId}")
    public void deleteMessage(@PathVariable Long chatId, @PathVariable Long msgId) {
        messageService.deleteMessage(chatId, msgId);
    }
}