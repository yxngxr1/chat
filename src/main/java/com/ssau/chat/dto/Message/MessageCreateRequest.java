package com.ssau.chat.dto.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание нового сообщения в чате")
public class MessageCreateRequest {

    @Schema(description = "Текст сообщения", example = "Привет, как дела?")
    @Size(min = 1, max = 10000, message = "Сообщение более 10000 символов")
    @NotBlank(message = "Message content is required")
    private String content;
}
