package com.ssau.chat.mapper;

import com.ssau.chat.dto.UserDTO;
import com.ssau.chat.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDto(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }

    public UserEntity toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return UserEntity.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .createdAt(userDTO.getCreatedAt())
                .build();
    }
}
