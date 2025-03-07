package com.ssau.chat.service;

import com.ssau.chat.dto.ChatDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.ChatUserEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.ChatMapper;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final ChatUserRepository chatUserRepository;

    public ChatDTO createChat(String name) {
        ChatEntity chat = ChatEntity.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();

        return chatMapper.toDto(chatRepository.save(chat));
    }

    public ChatDTO getChatById(Long id) {
        return chatRepository.findById(id)
                .map(chatMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    }

    public List<ChatDTO> getAllChats() {
        return chatRepository.findAll()
                .stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }

    public void addUserToChat(Long chatId, Long userId) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ChatUserEntity chatUser = ChatUserEntity.builder()
                .chat(chat)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();

        chatUserRepository.save(chatUser);
    }
}
