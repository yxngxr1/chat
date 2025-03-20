package com.ssau.chat.dto.Chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о чате")
public class ChatDTO {
    @Schema(description = "Идентификатор чата", example = "1")
    private Long id;

    @Schema(description = "Название чата", example = "Общий чат")
    private String name;

    @Schema(description = "Описание чата", example = "Чат для обсуждения общих вопросов")
    private String description;

    @Schema(description = "Дата и время создания чата", example = "2025-03-20T14:30:00")
    private LocalDateTime createdAt;
}
