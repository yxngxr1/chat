package com.ssau.chat.service;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.dto.RegistrationRequest;
import com.ssau.chat.dto.UserDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.ChatMapper;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ChatUserRepository chatUserRepository;

    public UserDTO createUser(RegistrationRequest registrationRequest) {

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        return UserMapper.toDto(savedUser);
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
}

