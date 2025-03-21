package com.ssau.chat.dto.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание нового сообщения в чате")
public class MessageCreateRequest {

    @NotNull(message = "Message content is required")
    @Schema(description = "Текст сообщения", example = "Привет, как дела?")
    private String content;
}
