package com.ssau.chat.dto.ChatUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Ответ при удалении пользователей из чата")
public class ChatUserDeleteResponse {

    @Schema(description = "Идентификатор чата", example = "1")
    @NotBlank
    private Long chatId;

    @Schema(description = "Список выгнанных идентификаторов пользователей из чата.", example = "[1, 2, 3]")
    private List<@Positive Long> userIds;
}
