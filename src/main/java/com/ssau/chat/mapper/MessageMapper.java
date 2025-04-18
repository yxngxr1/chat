package com.ssau.chat.mapper;

import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.MessageEntity;
import com.ssau.chat.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public static MessageDTO toDto(MessageEntity messageEntity) {
        if (messageEntity == null) {
            return null;
        }

        return MessageDTO.builder()
                .id(messageEntity.getId())
                .chatId(messageEntity.getChat().getId())
                .senderId(messageEntity.getSender().getId())
                .content(messageEntity.getContent())
                .sentAt(messageEntity.getSentAt())
                .updateAt(messageEntity.getUpdatedAt())
                .build();
    }

    public static MessageEntity toEntity(MessageDTO messageDTO, ChatEntity chatEntity, UserEntity sender) {
        if (messageDTO == null) {
            return null;
        }

        return MessageEntity.builder()
                .id(messageDTO.getId())
                .chat(chatEntity)
                .content(messageDTO.getContent())
                .sender(sender)
                .build();
    }
}
