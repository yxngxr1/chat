package com.ssau.chat.controller;

import com.ssau.chat.dto.MessageDTO;
import com.ssau.chat.dto.UpdateMessageRequest;
import com.ssau.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public MessageDTO sendMessage(@RequestBody MessageDTO messageDTO) {
        // TODO сделать dto по нормальному и вообще наверное убрать и сделать websocket
        return messageService.sendMessage(messageDTO);
    }

    @PutMapping("/chat/{chatId}/msg/{msgId}")
    public MessageDTO updateMessage(
            @PathVariable Long chatId, @PathVariable Long msgId,
            @RequestBody UpdateMessageRequest updateMessageRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug(updateMessageRequest.toString());
        return messageService.updateMessage(chatId, msgId, updateMessageRequest, userDetails);
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