package com.ssau.chat.service;

import com.ssau.chat.dto.Auth.LoginRequest;
import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.User.UserCreateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.dto.User.UserUpdateRequest;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.entity.enums.Role;
import com.ssau.chat.mapper.ChatMapper;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.UserRepository;
import com.ssau.chat.security.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatUserRepository chatUserRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse createUser(UserCreateRequest userCreateRequest) {

        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        if (userRepository.existsByUsername(userCreateRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(userCreateRequest.getUsername())
                .email(userCreateRequest.getEmail())
                .role(Role.ROLE_USER)
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return new LoginResponse(accessToken, refreshToken);

    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(String.format("User %d not found", userId));
        }
        userRepository.deleteById(userId);
    }

    public UserDTO getUserById(Long id) {
        UserEntity userEntity = userRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User with id %d not found", id)));
        return UserMapper.toDto(userEntity);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository
                .findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ChatDTO> getAllChatsById(Long id) {
        return chatUserRepository
                .findAllChatsByUserId(id).stream()
                .map(ChatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с именем " + username + " не найден"));
    }

    public List<UserDTO> findUsersByIds(Set<Long> ids) {
        return userRepository
                .findUsernamesByIds(ids).stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public LoginResponse updateUser(@Valid UserUpdateRequest userUpdateRequest, UserDetails userDetails) {
        UserEntity user = userRepository
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userUpdateRequest.getEmail() != null &&
                !userUpdateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userUpdateRequest.getEmail())) {
                throw new IllegalArgumentException("Email is already taken");
            }
            user.setEmail(userUpdateRequest.getEmail());
        }

        if (userUpdateRequest.getUsername() != null &&
                !userUpdateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userUpdateRequest.getUsername())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(userUpdateRequest.getUsername());
        }
        if (userUpdateRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        UserEntity updatedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(updatedUser);
        String refreshToken = jwtService.generateRefreshToken(updatedUser);

        return new LoginResponse(accessToken, refreshToken);
    }
}

