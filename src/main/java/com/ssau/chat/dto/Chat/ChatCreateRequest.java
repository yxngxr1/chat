package com.ssau.chat.dto.Chat;

import com.ssau.chat.entity.enums.ChatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос для создания нового чата")
public class ChatCreateRequest {

    @Schema(description = "Список идентификаторов пользователей для чата. Все ID должны быть положительными.", example = "[1, 2, 3]")
    private List<@Positive Long> userIds;

    @NotNull(message = "Chat type is required")
    @Schema(description = "Тип чата, который будет создан.", example = "GROUP")
    private ChatType chatType;
}
