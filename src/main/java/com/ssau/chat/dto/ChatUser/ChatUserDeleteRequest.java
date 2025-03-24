package com.ssau.chat.dto.ChatUser;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на удаление пользователей из чата")
public class ChatUserDeleteRequest {

    @Schema(description = "Список идентификаторов пользователей для чата. Все ID должны быть положительными.", example = "[1, 2, 3]")
    private List<@Positive Long> userIds;
}
