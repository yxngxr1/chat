package com.ssau.chat.dto.Message;

import lombok.Data;

@Data
public class MessageCreateRequest {
    private Long chatId;
    private String content;
}
