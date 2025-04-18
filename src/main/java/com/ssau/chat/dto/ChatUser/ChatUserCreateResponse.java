package com.ssau.chat.dto.ChatUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ при добавлении пользователя в чат")
public class ChatUserCreateResponse {
    @Schema(description = "Идентификатор чата", example = "1")
    @NotBlank
    private Long chatId;

    @Schema(description = "Список идентификаторов пользователей для чата. Все ID должны быть положительными.", example = "[1, 2, 3]")
    private List<@Positive Long> userIds;
}