package com.ssau.chat.mapper;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.entity.ChatEntity;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatDTO toDto(ChatEntity chatEntity) {
        if (chatEntity == null) {
            return null;
        }

        return ChatDTO.builder()
                .id(chatEntity.getId())
                .name(chatEntity.getName())
                .createdAt(chatEntity.getCreatedAt())
                .build();
    }

    public ChatEntity toEntity(ChatDTO chatDTO) {
        if (chatDTO == null) {
            return null;
        }

        return ChatEntity.builder()
                .id(chatDTO.getId())
                .name(chatDTO.getName())
                .createdAt(chatDTO.getCreatedAt())
                .build();
    }
}

