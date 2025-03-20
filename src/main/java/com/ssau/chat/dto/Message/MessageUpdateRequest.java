package com.ssau.chat.dto.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление содержания сообщения")
public class MessageUpdateRequest {
    @Schema(description = "Новый текст сообщения", example = "Обновленное сообщение")
    private String content;
}
