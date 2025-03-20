package com.ssau.chat.dto.Chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос для обновления информации о чате")
public class ChatUpdateRequest {
    @Schema(description = "Новое название чата", example = "Обновленный чат")
    private String name;

    @Schema(description = "Новое описание чата", example = "Чат для обсуждения актуальных вопросов")
    private String description;
}
