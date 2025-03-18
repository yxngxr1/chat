package com.ssau.chat.mapper;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.entity.ChatEntity;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public static ChatDTO toDto(ChatEntity chatEntity) {
        if (chatEntity == null) {
            return null;
        }

        return ChatDTO.builder()
                .id(chatEntity.getId())
                .name(chatEntity.getName())
                .description(chatEntity.getDescription())
                .createdAt(chatEntity.getCreatedAt())
                .build();
    }

    public static ChatEntity toEntity(ChatDTO chatDTO) {
        if (chatDTO == null) {
            return null;
        }

        return ChatEntity.builder()
                .id(chatDTO.getId())
                .name(chatDTO.getName())
                .description(chatDTO.getDescription())
                .createdAt(chatDTO.getCreatedAt())
                .build();
    }
}

