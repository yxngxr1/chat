package com.ssau.chat.service;

import com.ssau.chat.dto.Chat.ChatCreateRequest;
import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.Chat.ChatUpdateRequest;
import com.ssau.chat.dto.ChatUser.ChatUserCreateResponse;
import com.ssau.chat.dto.User.UserDTO;
import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.ChatUserEntity;
import com.ssau.chat.entity.UserEntity;
import com.ssau.chat.entity.enums.ChatType;
import com.ssau.chat.mapper.ChatMapper;
import com.ssau.chat.repository.ChatRepository;
import com.ssau.chat.repository.ChatUserRepository;
import com.ssau.chat.repository.MessageRepository;
import com.ssau.chat.repository.UserRepository;
import com.ssau.chat.service.utils.ChatServiceHelper;
import com.ssau.chat.service.utils.UserServiceHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatUserService chatUserService;

    private final UserServiceHelper userServiceHelper;
    private final ChatServiceHelper chatServiceHelper;

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatUserRepository chatUserRepository;
    private final MessageRepository messageRepository;

    public ChatDTO createChatWithUsers(ChatCreateRequest chatCreateRequest, UserDetails userDetails) {
        UserEntity creator = userServiceHelper.findUserByUsername(userDetails.getUsername());

        // Добавляем пользователей в чат в зависимости от типа
        ChatDTO chatDTO = switch (chatCreateRequest.getChatType()) {
            case PRIVATE -> createPrivateChat(chatCreateRequest, creator);
            case GROUP -> createGroupChat(chatCreateRequest, creator);
            case SELF -> createSelfChat(chatCreateRequest, creator);
        };

        return chatDTO;
    }

    public ChatDTO getChatById(Long chatId, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }

        return ChatMapper.toDto(chat);
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

        // не один и тот же челик
        if (creatorId.equals(secondUserId)) {
            throw new IllegalArgumentException("Creator cannot be the same as the user in private chat");
        }

        // проверка на существование
        UserEntity creatorUser = userServiceHelper.findUserById(creatorId);
        UserEntity user = userServiceHelper.findUserById(secondUserId);

        if (privateChatExists(creatorId, secondUserId)) {
            throw new IllegalArgumentException("Private chat already exists");
        }
        Set<Long> userIdsSet = new HashSet<>(Set.of(creatorId, secondUserId));
        ChatDTO chatDTO = createChat(chatCreateRequest, userIdsSet, creator);
        addUsersToChat(chatDTO.getId(), userIdsSet);

        return chatDTO;
    }

    private ChatDTO createGroupChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Set<Long> userIds = new HashSet<>(chatCreateRequest.getUserIds());
        userIds.add(creator.getId());

        ChatDTO chatDTO = createChat(chatCreateRequest, userIds, creator);
        addUsersToChat(chatDTO.getId(), userIds);

        return chatDTO;
    }

    private ChatDTO createSelfChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Long creatorId = creator.getId();

        if (getSelfChat(creatorId).isPresent()) {
            throw new IllegalArgumentException("Self chat already exists");
        }
        ChatDTO chatDTO = createChat(chatCreateRequest, new HashSet<>(Set.of(creatorId)), creator);
        addUsersToChat(chatDTO.getId(), new HashSet<>(Set.of(creatorId)));
        return chatDTO;
    }

    private ChatDTO createChat(ChatCreateRequest chatCreateRequest, Set<Long> userIds, UserEntity creator) {

        List<UserDTO> users = userServiceHelper.findUsersByIds(userIds);

        String chatName = users.stream()
                .map(UserDTO::getUsername)
                .collect(Collectors.joining(" "));

        ChatEntity chat = ChatEntity.builder()
                .name(chatName)
                .createdAt(LocalDateTime.now())
                .creator(creator)
                .type(chatCreateRequest.getChatType())
                .build();

        ChatEntity savedChat = chatRepository.save(chat);
        return ChatMapper.toDto(savedChat);
    }

    public ChatUserCreateResponse addUserToChat(Long chatId, Long userId, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }

        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException(String.format("Chat type %s is not supported adding users", chat.getType()));
        }

        UserEntity user = userServiceHelper.findUserById(userId);

        if (chatUserService.userInChat(chatId, user.getId())) {
            throw new IllegalArgumentException(String.format("User %d is already in the chat %d", userId, chatId));
        }

        ChatUserEntity chatUser = new ChatUserEntity(chat, user);
        log.debug("Create ChatUser {}", chatUser.getId());

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
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException(String.format("Chat type %s is not supported adding users", chat.getType()));
        }

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

    public void leaveUserFromChat(Long chatId, Long userId, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (!chatServiceHelper.userIsChatCreator(userDetails.getId(), chat.getCreator().getId())) {
            throw new AccessDeniedException("You are not creator of this chat");
        }

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }

        UserEntity userToLeave = userServiceHelper.findUserById(userId);

        ChatUserEntity existingChatUser = chatUserService.findChatUser(chatId, userToLeave.getId());

        chatUserRepository.delete(existingChatUser);
    }

    @Transactional
    public void deleteChat(Long chatId, UserDetails userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);
        UserEntity user = userServiceHelper.findUserByUsername(userDetails.getUsername());

        if (!chatServiceHelper.userIsChatCreator(user, chat)) {
            throw new AccessDeniedException("You are not creator of this chat");
        }

        chatRepository.delete(chat);

    }

    public List<ChatDTO> getAllChatsByUser(UserEntity userDetails) {
        return chatUserRepository.findAllChatsByUserId(userDetails.getId())
                .stream()
                .map(ChatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatDTO updateChat(Long chatId, ChatUpdateRequest chatUpdateRequest, UserDetails userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);
        UserEntity user = userServiceHelper.findUserByUsername(userDetails.getUsername());

        // является ли пользователь владельцем чата
        if (!chatServiceHelper.userIsChatCreator(user, chat)) {
            throw new AccessDeniedException("Only creator can edit");
        }

        log.debug(chatUpdateRequest.toString());
        chat.setName(chatUpdateRequest.getName());
        chat.setDescription(chatUpdateRequest.getDescription());

        ChatEntity updatedChat = chatRepository.save(chat);
        return ChatMapper.toDto(updatedChat);
    }



}
