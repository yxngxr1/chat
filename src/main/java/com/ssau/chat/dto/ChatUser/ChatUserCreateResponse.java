package com.ssau.chat.dto.ChatUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ при добавлении пользователя в чат")
public class ChatUserCreateResponse {
    @NotNull(message = "Chat ID is required")
    @Schema(description = "Идентификатор чата", example = "1")
    private Long chatId;

    @NotNull(message = "User ID is required")
    @Schema(description = "Идентификатор пользователя", example = "123")
    private Long userId;
}