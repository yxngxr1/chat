package com.ssau.chat.controller;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.Auth.LoginRequest;
import com.ssau.chat.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для входа и обновления токена")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Получение токена", description = "Аутентифицирует пользователя и возвращает токен доступа")
    @PostMapping("/access_token")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        return authService.authenticate(loginRequest);
    }

    @Operation(summary = "Обновление токена", description = "Обновляет токен доступа по refresh-токену")
    @PostMapping("/refresh_token")
    public LoginResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        return authService.refreshToken(request, response);
    }
}
