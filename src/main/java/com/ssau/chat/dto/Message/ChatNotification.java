package com.ssau.chat.dto.Message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatNotification {
    private Long chatId;
    private Long senderId;
    private String content;
}