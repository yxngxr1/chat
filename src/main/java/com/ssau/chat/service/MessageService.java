package com.ssau.chat.service;

import com.ssau.chat.dto.MessageDTO;
import com.ssau.chat.dto.UpdateMessageRequest;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.MessageEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.MessageMapper;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.repository.MessageRepository;
import com.ssau.chat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    public MessageDTO sendMessage(MessageDTO messageDTO) {
        ChatEntity chatEntity =
                chatRepository
                        .findById(messageDTO.getChatId())
                        .orElseThrow(() -> new IllegalArgumentException("Chat id not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            throw new SecurityException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        UserEntity sender = userRepository
                .findByUsername(username) // Предполагается метод поиска по username
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // TODO проверка пользователь находится в чате

        MessageEntity messageEntity = MessageMapper.toEntity(messageDTO, chatEntity, sender);
        MessageEntity savedMessage = messageRepository.save(messageEntity);
        return MessageMapper.toDto(savedMessage);
    }

    public List<MessageDTO> getMessagesByChat(Long chatId) {
        return messageRepository.findByChat_Id(chatId).stream()
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteMessage(Long chatId, Long msgId) {
        ChatEntity chatEntity =
                chatRepository
                        .findById(chatId)
                        .orElseThrow(() -> new IllegalArgumentException("Chat id not found"));

        MessageEntity message =
                messageRepository
                        .findById(msgId)
                        .orElseThrow(() -> new IllegalArgumentException("Message id not found"));


        // TODO сообщение может удалить юзер находящийся в этом чате

        // TODO сообщение может удалить только владелец

        messageRepository.delete(message);
    }

    public MessageDTO updateMessage(Long chatId, Long msgId, UpdateMessageRequest updateMessageRequest, UserDetails userDetails) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with id: " + chatId));

        MessageEntity message = messageRepository.findById(msgId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + msgId));

        UserEntity creator = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        if (creator.getId() != message.getSender().getId()) {
            throw new SecurityException("Only the sender can change the message");
        }

        message.setContent(updateMessageRequest.getContent());

        MessageEntity updatedMessage = messageRepository.save(message);
        return MessageMapper.toDto(updatedMessage);
    }
}

