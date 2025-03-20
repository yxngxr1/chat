package com.ssau.chat.dto.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление данных пользователя, включая имя, email и пароль")
public class UserUpdateRequest {
    @Schema(description = "Имя пользователя", example = "Jon")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    @Schema(description = "Адрес электронной почты", example = "jondoe@gmail.com")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Пароль", example = "my_1secret1_password")
    @Size(min = 8, max=255, message = "Password must be at least 8 characters")
    private String password;
}
