package com.ssau.chat.service;

import com.ssau.chat.dto.Message.MessageCreateRequest;
import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.Message.MessageUpdateRequest;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.MessageEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.MessageMapper;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.MessageRepository;
import com.ssau.chat.repository.UserRepository;
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
    private final ChatUserRepository chatUserRepository;

    public MessageDTO sendMessage(MessageCreateRequest messageCreateRequest) {
        ChatEntity chatEntity =
                chatRepository
                        .findById(messageCreateRequest.getChatId())
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
        if (chatUserRepository
                .findByChatIdAndUserId(chatEntity.getId(), sender.getId())
                .isEmpty()) {
            throw new IllegalArgumentException(String.format("User %d is not in chat %d", sender.getId(), chatEntity.getId()));
        }

        MessageDTO messageDTO = MessageDTO.builder()
                .chatId(messageCreateRequest.getChatId())
                .content(messageCreateRequest.getContent())
                .build();
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

        // TODO сообщение может удалить только владелец сообщения

        messageRepository.delete(message);
    }

    public MessageDTO updateMessage(Long chatId, Long msgId, MessageUpdateRequest messageUpdateRequest, UserDetails userDetails) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with id: " + chatId));

        MessageEntity message = messageRepository.findById(msgId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + msgId));

        UserEntity creator = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        if (creator.getId() != message.getSender().getId()) {
            throw new SecurityException("Only the sender can change the message");
        }

        message.setContent(messageUpdateRequest.getContent());

        MessageEntity updatedMessage = messageRepository.save(message);
        return MessageMapper.toDto(updatedMessage);
    }
}

