package com.ssau.chat.dto.Chat;

import com.ssau.chat.entity.enums.ChatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о чате")
public class ChatDTO {
    @NotBlank
    @Schema(description = "Идентификатор чата", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Название чата", example = "Общий чат")
    private String name;

    @NotBlank
    @Schema(description = "Описание чата", example = "Чат для обсуждения общих вопросов")
    private String description;

    @NotBlank
    @Schema(description = "Создатель чата", example = "1")
    private Long creatorId;

    @NotBlank
    @Schema(description = "Тип чата")
    private ChatType type;

    @NotBlank
    @Schema(description = "Дата и время создания чата", example = "2025-03-20T14:30:00")
    private LocalDateTime createdAt;
}
