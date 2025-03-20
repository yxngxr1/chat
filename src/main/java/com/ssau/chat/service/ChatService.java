package com.ssau.chat.service;

import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.Chat.ChatCreateRequest;
import com.ssau.chat.dto.Chat.ChatUpdateRequest;
import com.ssau.chat.dto.ChatUser.ChatUserCreateResponse;
import com.ssau.chat.dto.Message.MessageDTO;
import com.ssau.chat.dto.User.UserDTO;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    public ChatDTO createChatWithUsers(ChatCreateRequest chatCreateRequest, UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        // Добавляем пользователей в чат в зависимости от типа
        ChatDTO chatDTO = switch (chatCreateRequest.getChatType()) {
            case PRIVATE -> createPrivateChat(chatCreateRequest, creator);
            case GROUP -> createGroupChat(chatCreateRequest, creator);
            case SELF -> createSelfChat(chatCreateRequest, creator);
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

    private ChatDTO createPrivateChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Long creatorId = creator.getId();
        List<Long> userIds = chatCreateRequest.getUserIds();

        // Проверяем, что у нас ровно один user в списке (кроме создателя)
        if (userIds == null || userIds.size() != 1) {
            throw new IllegalArgumentException("Private chat must have exactly one other user");
        }
        Long secondUserId = userIds.getFirst();

        // один и тот же челик
        if (creatorId.equals(secondUserId)) {
            throw new IllegalArgumentException("Creator cannot be the same as the user in private chat");
        }

        // проверка на существование
        UserDTO creatorUserDTO = userService.getUserById(creatorId);
        UserDTO userDTO = userService.getUserById(secondUserId);

        if (privateChatExists(creatorId, secondUserId)) {
            throw new IllegalArgumentException("Private chat already exists");
        }
        Set<Long> userIdsSet = new HashSet<>(Set.of(creatorId, secondUserId));
        ChatDTO chatDTO = createChat(chatCreateRequest, userIdsSet);
        addUsersToChat(chatDTO.getId(), userIdsSet);

        return chatDTO;
    }

    private ChatDTO createGroupChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Set<Long> userIds = new HashSet<>(chatCreateRequest.getUserIds());
        userIds.add(creator.getId());

        ChatDTO chatDTO = createChat(chatCreateRequest, userIds);
        addUsersToChat(chatDTO.getId(), userIds);

        return chatDTO;
    }

    private ChatDTO createSelfChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Long creatorId = creator.getId();

        if (getSelfChat(creatorId).isPresent()) {
            throw new IllegalArgumentException("Self chat already exists");
        }
        ChatDTO chatDTO = createChat(chatCreateRequest, new HashSet<>(Set.of(creatorId)));

        addUserToChat(chatDTO.getId(), creatorId);

        return chatDTO;
    }

    private ChatDTO createChat(ChatCreateRequest chatCreateRequest, Set<Long> userIds) {

        List<UserDTO> users = userService.findUsersByIds(userIds);

        String chatName = users.stream()
                .map(UserDTO::getUsername)
                .collect(Collectors.joining(" "));

        ChatEntity chat = ChatEntity.builder()
                .name(chatName)
                .createdAt(LocalDateTime.now())
                .type(chatCreateRequest.getChatType())
                .build();

        ChatEntity savedChat = chatRepository.save(chat);
        return ChatMapper.toDto(savedChat);
    }

    public ChatUserCreateResponse addUserToChat(Long chatId, Long userId) {
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

        ChatUserCreateResponse chatUserCreateResponse = ChatUserCreateResponse.builder()
                .chatId(savedChatUser.getChat().getId())
                .userId(savedChatUser.getUser().getId())
                .joinedAt(savedChatUser.getJoinedAt())
                .build();

        return chatUserCreateResponse;
    }

    public void addUsersToChat(Long chatId, Set<Long> userIds) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat id not found"));

        List<UserEntity> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new IllegalArgumentException("Some user IDs are invalid");
        }

        // Получаем уже существующих участников чата
        Set<Long> existingUserIds = chatUserRepository.findByChatId(chatId).stream()
                .map(chatUser -> chatUser.getUser().getId())
                .collect(Collectors.toSet());

        List<ChatUserEntity> newUsers = new ArrayList<>();
        for (UserEntity user : users) {
            if (!existingUserIds.contains(user.getId())) {
                newUsers.add(new ChatUserEntity(chat, user));
            }
        }

        if (!newUsers.isEmpty()) {
            chatUserRepository.saveAll(newUsers); // Сохраняем всех за раз
            log.info("Added {} new users to chat {}", newUsers.size(), chatId);
        }
    }


    public boolean privateChatExists(Long userId1, Long userId2) {
        Optional<ChatEntity> chat = chatUserRepository.findPrivateChatBetweenUsers(userId1, userId2); // или Ordered, или JPQL версия
        return chat.isPresent();
    }

    public Optional<ChatEntity> getSelfChat(Long userId) {
        return chatUserRepository.findSelfChat(userId);
    }

    public List<UserDTO> getAllUsersByChatId(Long id) {

        ChatEntity chat = chatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chat id not found"));

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

        ChatUserEntity existingChatUser =
                chatUserRepository
                        .findByChatIdAndUserId(chatId, userId)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("User %d is not in chat", userId)));

        chatUserRepository.delete(existingChatUser);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        // TODO как то не круто

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

        chatRepository.deleteById(chatId);

    }

    public List<ChatDTO> getAllChatsByUser(UserEntity user) {
        return chatUserRepository.findAllChatsByUserId(user.getId())
                .stream()
                .map(ChatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatDTO updateChat(Long id, ChatUpdateRequest chatUpdateRequest, UserDetails userDetails) {
        ChatEntity chat = chatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id: " + id));

        // TODO Проверка, является ли пользователь владельцем чата (если есть такое требование)
        log.debug(chatUpdateRequest.toString());
        chat.setName(chatUpdateRequest.getName());
        chat.setDescription(chatUpdateRequest.getDescription());

        ChatEntity updatedChat = chatRepository.save(chat);
        return ChatMapper.toDto(updatedChat);
    }
}
