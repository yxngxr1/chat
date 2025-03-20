package com.ssau.chat.controller;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.User.UserCreateRequest;
import com.ssau.chat.dto.Chat.ChatUpdateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.dto.User.UserUpdateRequest;
import com.ssau.chat.service.AuthService;
import com.ssau.chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Создание нового пользователя", description = "Создает нового пользователя и возвращает ответ с токеном")
    @PostMapping
    public LoginResponse createUser(
            @RequestBody @Valid UserCreateRequest userCreateRequest) {
        return userService.createUser(userCreateRequest);
    }

    @Operation(summary = "Обновление данных пользователя", description = "Обновляет информацию о пользователе")
    @PutMapping
    public LoginResponse updateUser(
            @RequestBody @Valid UserUpdateRequest userUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateUser(userUpdateRequest, userDetails);
    }

    @Operation(summary = "Удаление пользователя", description = "Удаляет пользователя по ID")
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }

    @Operation(summary = "Получение информации о пользователе", description = "Возвращает информацию о пользователе по его ID")
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Получение всех пользователей", description = "Возвращает список всех пользователей")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Получение чатов пользователя", description = "Возвращает все чаты пользователя по его ID")
    @GetMapping("/{id}/chats")
    public List<ChatDTO> getAllChatsById(@PathVariable Long id) {
        return userService.getAllChatsById(id);
    }
}
