package com.ssau.chat.dto.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Представление данных пользователя, включая ID, имя, email и дату создания")
public class UserDTO {
    @NotNull
    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Имя пользователя", example = "Jon")
    private String username;

    @NotBlank
    @Schema(description = "Адрес электронной почты", example = "jondoe@gmail.com")
    private String email;

    @NotBlank
    @Schema(description = "Дата и время создания пользователя", example = "2025-03-20T12:00:00")
    private LocalDateTime createdAt;
}

