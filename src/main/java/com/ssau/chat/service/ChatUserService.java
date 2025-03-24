package com.ssau.chat.service;

import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.ChatUserEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.ChatUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatUserService {

    private final ChatUserRepository chatUserRepository;

    public ChatUserEntity findChatUser(Long chatId, Long userId){
        return chatUserRepository
                .findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User %d not in chat: %d", userId, chatId)));
    }

    public boolean userInChat(ChatEntity chat, UserEntity user){
        return chatUserRepository.findByChatIdAndUserId(chat.getId(), user.getId()).isPresent();
    }

    public boolean userInChat(Long chatId, Long userId){
        return chatUserRepository.findByChatIdAndUserId(chatId, userId).isPresent();
    }

    public List<UserDTO> findAllUsersByChatId(Long chatId) {
        return chatUserRepository
                .findAllUsersByChatId(chatId).stream()
                .map(UserMapper::toDto)
                .toList();
    }
}
