package com.ssau.chat.controller;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.dto.RegistrationRequest;
import com.ssau.chat.dto.UserDTO;
import com.ssau.chat.service.AuthService;
import com.ssau.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public UserDTO createUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        return authService.createUser(registrationRequest);
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
