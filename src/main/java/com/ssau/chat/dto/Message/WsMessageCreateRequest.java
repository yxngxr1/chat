package com.ssau.chat.dto.Message;

import lombok.Data;

@Data
public class WsMessageCreateRequest {
    private Long chatId;
    private Long senderId;
    private String content;
}
