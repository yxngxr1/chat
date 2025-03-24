package com.ssau.chat.service;

import com.ssau.chat.dto.Message.MessageCreateRequest;
import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.Message.MessageUpdateRequest;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.MessageEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.MessageMapper;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.MessageRepository;
import com.ssau.chat.repository.UserRepository;
import com.ssau.chat.service.utils.ChatServiceHelper;
import com.ssau.chat.service.utils.UserServiceHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;

    private final ChatUserService chatUserService;

    private final UserServiceHelper userServiceHelper;
    private final ChatServiceHelper chatServiceHelper;

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatUserRepository chatUserRepository;

    public MessageDTO sendMessage(Long chatId, MessageCreateRequest messageCreateRequest, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);
        UserEntity user = userServiceHelper.findUserById(userDetails.getId());

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("Тебя нет в этом чате");
        }

        MessageEntity messageEntity = MessageEntity.builder()
                .chat(chat)
                .sender(user)
                .content(messageCreateRequest.getContent())
                .build();
        MessageEntity savedMessage = messageRepository.save(messageEntity);

        return MessageMapper.toDto(savedMessage);
    }

    public MessageDTO createMessage(Long chatId, Long senderId, String content) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);
        UserEntity user = userServiceHelper.findUserById(senderId);

        MessageEntity messageEntity = MessageEntity.builder()
                .chat(chat)
                .sender(user)
                .content(content)
                .build();
        MessageEntity savedMessage = messageRepository.save(messageEntity);

        return MessageMapper.toDto(savedMessage);

    }

    public MessageDTO updateMessage(Long chatId, Long msgId, MessageUpdateRequest messageUpdateRequest, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);
        MessageEntity message = findMessageById(msgId);

        if (!userIsMessageSender(userDetails.getId(), message.getSender().getId())) {
            throw new AccessDeniedException("Только отправитель может изменять сообщение");
        }

        message.setContent(messageUpdateRequest.getContent());
        MessageEntity updatedMessage = messageRepository.save(message);

        return MessageMapper.toDto(updatedMessage);
    }

    public void deleteMessage(Long chatId, Long msgId, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);
        MessageEntity message = findMessageById(msgId);

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("Тебя нет в этом чате");
        }

        if (!userIsMessageSender(userDetails.getId(), message.getSender().getId())) {
            throw new AccessDeniedException("Только отправитель может удалить сообщение");
        }

        messageRepository.delete(message);
    }

    public List<MessageDTO> getMessagesByChat(Long chatId, UserEntity userDetails) {

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("Тебя нет в этом чате");
        }

        return messageRepository.findByChat_Id(chatId).stream()
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
    }

    public MessageEntity findMessageById(Long msgId) {
        return messageRepository
                .findById(msgId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + msgId));
    }

    public boolean userIsMessageSender(UserEntity user, MessageEntity message) {
        return message.getSender().getId().equals(user.getId());
    }

    public boolean userIsMessageSender(Long userId, Long messageId) {
        return messageId.equals(userId);
    }
}

