package com.ssau.chat.service;

import com.ssau.chat.dto.Auth.LoginResponse;
import com.ssau.chat.dto.User.UserCreateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.dto.User.UserUpdateRequest;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.entity.enums.Role;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.UserRepository;
import com.ssau.chat.security.service.JwtService;
import com.ssau.chat.service.utils.ChatServiceHelper;
import com.ssau.chat.service.utils.UserServiceHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private final ChatUserService chatUserService;

    private final ChatServiceHelper chatServiceHelper;
    private final UserServiceHelper userServiceHelper;

    private final UserRepository userRepository;

    public LoginResponse createUser(UserCreateRequest userCreateRequest) {

        if (userServiceHelper.userExistByUsername(userCreateRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userServiceHelper.userExistByEmail(userCreateRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
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

    public LoginResponse updateUser(UserUpdateRequest userUpdateRequest, UserDetails userDetails) {
        // access проверять вроде не надо ибо userDetails берется через контекст

        UserEntity user = userServiceHelper.findUserByUsername(userDetails.getUsername());

        if (userUpdateRequest.getUsername() != null &&
                !userUpdateRequest.getUsername().equals(user.getUsername())) {
            if (userServiceHelper.userExistByUsername(userUpdateRequest.getUsername())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(userUpdateRequest.getUsername());
        }

        if (userUpdateRequest.getEmail() != null &&
                !userUpdateRequest.getEmail().equals(user.getEmail())) {
            if (userServiceHelper.userExistByEmail(userUpdateRequest.getEmail())) {
                throw new IllegalArgumentException("Email is already taken");
            }
            user.setEmail(userUpdateRequest.getEmail());
        }

        if (userUpdateRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        UserEntity updatedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(updatedUser);
        String refreshToken = jwtService.generateRefreshToken(updatedUser);

        return new LoginResponse(accessToken, refreshToken);
    }

    public void deleteUserById(Long userId) {
        UserEntity user = userServiceHelper.findUserById(userId);
        userRepository.deleteById(user.getId());
    }

    public void deleteMe(UserEntity userDetails) {
        UserEntity user = userServiceHelper.findUserByUsername(userDetails.getUsername());
        userRepository.delete(user);
    }

    public UserDTO getUserById(Long id) {
        UserEntity user = userServiceHelper.findUserById(id);
        return UserMapper.toDto(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository
                .findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String keyword) {
        return userRepository
                .searchByUsername(keyword).stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsersByChatId(Long chatId, UserEntity userDetails) {
        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }
        
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        return chatUserService.findAllUsersByChatId(chatId);
    }

    public UserDetails loadUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с именем " + username + " не найден"));
    }
}
