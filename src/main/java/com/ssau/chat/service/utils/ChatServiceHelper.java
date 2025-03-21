package com.ssau.chat.service.utils;

import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.repository.ChatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatServiceHelper {

    private final ChatRepository chatRepository;

    public ChatEntity findChatById(Long chatId) {
        return chatRepository
                .findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id: " + chatId));
    }

    public boolean userIsChatCreator(UserEntity user, ChatEntity chat){
        return Objects.equals(chat.getCreator().getId(), user.getId());
    }

    public boolean userIsChatCreator(Long userId, Long creatorId) {
        return Objects.equals(userId, creatorId);
    }

}
