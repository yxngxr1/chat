package com.ssau.chat.dto.Message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long chatId;    // ID чата
    private Long senderId;  // ID отправителя
    private String content; // Текст сообщения
    private LocalDateTime sentAt; // Время отправки
}

