package com.ssau.chat.dto.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WsMessageCreateRequest {

    @NotNull(message = "Chat ID is required")
    private Long chatId;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @Schema(description = "Текст сообщения", example = "Привет, как дела?")
    @NotBlank(message = "Сообщение должно содержать хотя бы один символ")
    @Size(max = 10000, message = "Сообщение не может превышать 10000 символов")
    private String content;
}
