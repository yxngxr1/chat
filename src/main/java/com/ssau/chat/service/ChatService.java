package com.ssau.chat.service;

import com.ssau.chat.dto.*;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.ChatUserEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.mapper.ChatMapper;
import com.ssau.chat.mapper.MessageMapper;
import com.ssau.chat.mapper.UserMapper;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.MessageRepository;
import com.ssau.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    @Autowired
    private UserService userService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatUserRepository chatUserRepository;

    @Autowired
    private MessageRepository messageRepository;

    public ChatDTO createChatWithUsers(CreateChatRequest createChatRequest) {

        // Добавляем пользователей в чат в зависимости от типа
        ChatDTO chatDTO = switch (createChatRequest.getChatType()) {
            case PRIVATE -> createPrivateChat(createChatRequest);
            case GROUP -> createGroupChat(createChatRequest);
            case SELF -> createSelfChat(createChatRequest);
        };

        return chatDTO;
    }

    public ChatDTO getChatById(Long id) {
        ChatEntity chatEntity =
                chatRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Chat id not found"));
        return ChatMapper.toDto(chatEntity);
    }

    public List<ChatDTO> getAllChats() {
        return chatRepository.findAll()
                .stream()
                .map(ChatMapper::toDto)
                .collect(Collectors.toList());
    }

    private ChatDTO createPrivateChat(CreateChatRequest createChatRequest) {
        Long creatorId = createChatRequest.getCreatorId();
        List<Long> userIds = createChatRequest.getUserIds();

        // Проверяем, что у нас ровно один user в списке (кроме создателя)
        if (userIds == null || userIds.size() != 1) {
            throw new IllegalArgumentException("Private chat must have exactly one other user");
        }

        // создатель - не его собеседник
        if (creatorId.equals(userIds.getFirst())) {
            throw new IllegalArgumentException("Creator cannot be the same as the user in private chat");
        }

        // проверка на существование
        UserDTO creatorUserDTO = userService.getUserById(creatorId);
        UserDTO userDTO = userService.getUserById(userIds.getFirst());

        if (privateChatExists(creatorId, userIds.getFirst())) {
            throw new IllegalArgumentException("Private chat already exists");
        }

        ChatDTO chatDTO = createChat(createChatRequest);

        Long otherUserId = userIds.getFirst();
        addUserToChat(chatDTO.getId(), creatorId);
        addUserToChat(chatDTO.getId(), otherUserId);

        return chatDTO;
    }

    private ChatDTO createGroupChat(CreateChatRequest createChatRequest) {
        Long creatorId = createChatRequest.getCreatorId();
        List<Long> userIds = createChatRequest.getUserIds();

        ChatDTO chatDTO = createChat(createChatRequest);

        addUserToChat(chatDTO.getId(), creatorId);
        if (userIds != null) {
            // TODO наверное надо оптимизировать
            for (Long userId : userIds) {
                addUserToChat(chatDTO.getId(), userId);
            }
        }
        return chatDTO;
    }

    private ChatDTO createSelfChat(CreateChatRequest createChatRequest) {
        Long creatorId = createChatRequest.getCreatorId();

        if (getSelfChat(createChatRequest.getCreatorId()).isPresent()) {
            throw new IllegalArgumentException("Self chat already exists");
        }
        ChatDTO chatDTO = createChat(createChatRequest);

        addUserToChat(chatDTO.getId(), creatorId);

        return chatDTO;
    }

    private ChatDTO createChat(CreateChatRequest createChatRequest) {

        ChatEntity chat = ChatEntity.builder()
                .name(String.valueOf(createChatRequest.getCreatorId())) //todo change to real name for group chats
                .createdAt(LocalDateTime.now())
                .type(createChatRequest.getChatType())
                .build();

        ChatEntity savedChat = chatRepository.save(chat);
        return ChatMapper.toDto(savedChat);
    }

    public ChatUserJoinResponse addUserToChat(Long chatId, Long userId) {
        ChatEntity chat =
                chatRepository
                        .findById(chatId)
                        .orElseThrow(() -> new IllegalArgumentException("Chat id not found"));
        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User id not found"));

        Optional<ChatUserEntity> existingUser = chatUserRepository.findByChatIdAndUserId(chatId, userId);
        if (existingUser.isPresent()) {
            log.debug("Find ChatUser {}", existingUser.get().getId());
            throw new IllegalArgumentException(String.format("User %d is already in the chat %d", userId, chatId));
        }

        ChatUserEntity chatUser = new ChatUserEntity(chat, user);
        log.debug("Create ChatUser {}", chatUser.getId());

        // TODO если PRIVATE или SELF нельзя добавить (если уже сформировано)

        ChatUserEntity savedChatUser = chatUserRepository.save(chatUser);

        log.info("Adding user {} to chat {}", userId, chatId);

        ChatUserJoinResponse chatUserJoinResponse = ChatUserJoinResponse.builder()
                .chatId(savedChatUser.getChat().getId())
                .userId(savedChatUser.getUser().getId())
                .joinedAt(savedChatUser.getJoinedAt())
                .build();

        return chatUserJoinResponse;
    }

    public boolean privateChatExists(Long userId1, Long userId2) {
        Optional<ChatEntity> chat = chatUserRepository.findPrivateChatBetweenUsers(userId1, userId2); // или Ordered, или JPQL версия
        return chat.isPresent();
    }

    public Optional<ChatEntity> getSelfChat(Long userId) {
        return chatUserRepository.findSelfChat(userId);
    }

    public List<UserDTO> getAllUsersByChatId(Long id) {
        return chatUserRepository
                .findAllUsersByChatId(id).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    public void leaveUserFromChat(Long chatId, Long userId) {
        ChatEntity chat =
                chatRepository
                        .findById(chatId)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("Chat %d not found", chatId)));
        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("User %d not found", userId)));

        ChatUserEntity existingUser =
                chatUserRepository
                        .findByChatIdAndUserId(chatId, userId)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("User %d is not in chat", userId)));

        chatUserRepository.delete(existingUser);
    }

    public void deleteChat(Long chatId) {
        ChatEntity chat =
                chatRepository
                        .findById(chatId)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("Chat %d not found", chatId)));

        List<UserDTO> userDTOList =
                chatUserRepository
                        .findAllUsersByChatId(chatId).stream()
                        .map(UserMapper::toDto)
                        .toList();

        log.debug("users in chat: {}", userDTOList.size());

        int c = chatUserRepository.deleteAllByChatId(chatId);
        log.debug("users deleted: {}", c);

        List<MessageDTO> messageDTOList =
                messageRepository
                        .findByChat_Id(chatId).stream()
                        .map(MessageMapper::toDto)
                        .toList();
//
        log.debug("messages in chat: {}", messageDTOList.size());

        c = messageRepository.deleteAllByChatId(chatId);
        log.debug("messages deleted: {}", c);

        // TODO схуято со второго раза только работает
        chatRepository.deleteById(chatId);

    }
}
