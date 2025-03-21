package com.ssau.chat.service.utils;

import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceHelper {

    private final UserRepository userRepository;


    public List<UserDTO> findUsersByIds(Set<Long> ids) {
        return userRepository
                .findUsernamesByIds(ids).stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }


    public UserEntity findUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with name: " + username));
    }

    public UserEntity findUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    public UserEntity findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Email is already taken"));
    }

    public Boolean userExistByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean userExistByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
