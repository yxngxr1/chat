package com.ssau.chat.controller;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.Auth.LoginRequest;
import com.ssau.chat.service.AuthService;
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
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        return authService.authenticate(loginRequest);
    }

    @PostMapping("/refresh_token")
    public LoginResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        return authService.refreshToken(request, response);
    }
}
