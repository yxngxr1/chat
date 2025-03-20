package com.ssau.chat.dto.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Представление сообщения в чате, включая ID, чат, отправителя, текст и время отправки")
public class MessageDTO {
    @Schema(description = "Идентификатор сообщения", example = "1")
    private Long id;

    @Schema(description = "Идентификатор чата, в котором находится сообщение", example = "1")
    private Long chatId;    // ID чата

    @Schema(description = "Идентификатор отправителя сообщения", example = "123")
    private Long senderId;  // ID отправителя

    @Schema(description = "Текст сообщения", example = "Привет, как дела?")
    private String content; // Текст сообщения

    @Schema(description = "Дата и время отправки сообщения", example = "2025-03-20T14:30:00")
    private LocalDateTime sentAt; // Время отправки
}

