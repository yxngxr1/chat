package com.ssau.chat.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на получение токена")
public class LoginRequest {
    @Schema(description = "Имя пользователя", example = "Jon")
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    @Schema(description = "Пароль", example = "my_1secret1_password")
    @NotBlank(message = "Password is required")
    @Size(min = 3, max=255, message = "Password must be at least 8 characters")
    private String password;
}