package com.ssau.chat.controller;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.User.UserCreateRequest;
import com.ssau.chat.dto.Chat.ChatUpdateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.dto.User.UserUpdateRequest;
import com.ssau.chat.service.AuthService;
import com.ssau.chat.service.UserService;
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
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public LoginResponse createUser(
            @RequestBody @Valid UserCreateRequest userCreateRequest) {
        return userService.createUser(userCreateRequest);
    }

    @PutMapping
    public LoginResponse updateUser(
            @RequestBody @Valid UserUpdateRequest userUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateUser(userUpdateRequest, userDetails);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}/chats")
    public List<ChatDTO> getAllChatsById(@PathVariable Long id) {
        return userService.getAllChatsById(id);
    }
}
