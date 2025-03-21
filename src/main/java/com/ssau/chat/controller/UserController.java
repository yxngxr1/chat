package com.ssau.chat.controller;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.User.UserCreateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.dto.User.UserUpdateRequest;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

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
            @AuthenticationPrincipal UserEntity userDetails) {
        return userService.updateUser(userUpdateRequest, userDetails);
    }

    @Operation(summary = "Удаление пользователя", description = "Удаляет пользователя по ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public void deleteUser(
            @PathVariable Long userId) {
        userService.deleteUserById(userId);
    }

    @Operation(summary = "Удаление пользователя (себя)", description = "Удаляет пользователя по контексту")
    @DeleteMapping("/me")
    public void deleteUser(
            @AuthenticationPrincipal UserEntity userDetails) {
        userService.deleteMe(userDetails);
    }

    @Operation(summary = "Получение информации о пользователе", description = "Возвращает информацию о пользователе по его ID")
    @GetMapping("/{userId}")
    public UserDTO getUserById(
            @PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @Operation(summary = "Получение информации о себе", description = "Возвращает информацию о пользователе по его токену")
    @GetMapping("/me")
    public UserDTO getMe(
            @AuthenticationPrincipal UserEntity userDetails) {
        return userService.getUserById(userDetails.getId());
    }

    @Operation(summary = "Получение всех пользователей", description = "Возвращает список всех пользователей")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Получить список пользователей чата", description = "Возвращает список пользователей, которые состоят в чате")
    @GetMapping("/{chatId}/users")
    public List<UserDTO> getUsersByChatId(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserEntity userDetails) {
        return userService.getAllUsersByChatId(chatId, userDetails);
    }

    @GetMapping("/my_role")
    public List<String> getUserRoles(
            @AuthenticationPrincipal UserEntity userDetails) {
        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
