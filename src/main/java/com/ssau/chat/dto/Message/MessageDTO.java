package com.ssau.chat.dto.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Представление сообщения в чате, включая ID, чат, отправителя, текст и время отправки")
public class MessageDTO {
    @NotBlank
    @Schema(description = "Идентификатор сообщения", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Идентификатор чата, в котором находится сообщение", example = "1")
    private Long chatId;    // ID чата

    @NotBlank
    @Schema(description = "Идентификатор отправителя сообщения", example = "123")
    private Long senderId;  // ID отправителя

    @NotBlank
    @Schema(description = "Текст сообщения", example = "Привет, как дела?")
    private String content; // Текст сообщения

    @NotBlank
    @Schema(description = "Дата и время отправки сообщения", example = "2025-03-20T14:30:00")
    private LocalDateTime sentAt; // Время отправки
}

