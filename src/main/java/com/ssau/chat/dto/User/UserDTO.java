package com.ssau.chat.dto.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Представление данных пользователя, включая ID, имя, email и дату создания")
public class UserDTO {
    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "Jon")
    private String username;

    @Schema(description = "Адрес электронной почты", example = "jondoe@gmail.com")
    private String email;

    @Schema(description = "Дата и время создания пользователя", example = "2025-03-20T12:00:00")
    private LocalDateTime createdAt;
}

