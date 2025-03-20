package com.ssau.chat.dto.ChatUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ при добавлении пользователя в чат")
public class ChatUserCreateResponse {
    @Schema(description = "Идентификатор чата", example = "1")
    @NotBlank
    private Long chatId;

    @Schema(description = "Идентификатор пользователя", example = "123")
    @NotBlank
    private Long userId;

    @NotBlank
    @Schema(description = "Дата и время присоединения пользователя к чату", example = "2025-03-20T14:30:00")
    private LocalDateTime joinedAt;
}