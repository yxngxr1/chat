package com.ssau.chat.service;

import com.ssau.chat.dto.JwtAuthenticationResponse;
import com.ssau.chat.dto.LoginRequest;
import com.ssau.chat.dto.RegistrationRequest;
import com.ssau.chat.dto.UserDTO;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.entity.enums.Role;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.UserRepository;
import com.ssau.chat.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;


    public UserDTO createUser(RegistrationRequest registrationRequest) {

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .role(Role.ROLE_USER)
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        return UserMapper.toDto(savedUser);
    }

    public JwtAuthenticationResponse authenticate(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserEntity user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new JwtAuthenticationResponse(accessToken, refreshToken);
    }

    public JwtAuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("No JWT token");
        }
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(token);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));
        if (jwtService.validate(token)) {
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            return new JwtAuthenticationResponse(accessToken, refreshToken);
        }
        return null;
    }
}
