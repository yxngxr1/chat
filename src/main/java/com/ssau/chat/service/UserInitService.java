package com.ssau.chat.service;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.User.UserCreateRequest;
import com.ssau.chat.service.utils.UserServiceHelper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInitService {

    private final UserServiceHelper userServiceHelper;
    private final UserService userService;

    @PostConstruct
    public void initDefaultUser() {
        String username = "godji";
        String email = "1@1.ru";
        String password = "123";

        if (!userServiceHelper.userExistByUsername(username)) {
            UserCreateRequest request = new UserCreateRequest(username, email, password);
            LoginResponse response = userService.createUser(request);
            log.info("User '{}' created. Token: {}", username, response.getAccessToken());
        } else {
            log.info("User '{}' already exists.", username);
        }
    }
}