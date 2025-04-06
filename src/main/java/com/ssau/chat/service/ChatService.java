package com.ssau.chat.service;

import com.ssau.chat.dto.Chat.ChatCreateRequest;
import com.ssau.chat.dto.Chat.ChatDTO;
import com.ssau.chat.dto.Chat.ChatUpdateRequest;
import com.ssau.chat.dto.ChatUser.ChatUserCreateRequest;
import com.ssau.chat.dto.ChatUser.ChatUserCreateResponse;
import com.ssau.chat.dto.ChatUser.ChatUserDeleteRequest;
import com.ssau.chat.dto.ChatUser.ChatUserDeleteResponse;
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
        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }

        ChatEntity chat = chatServiceHelper.findChatById(chatId);

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
        UserEntity user = userServiceHelper.findUserById(secondUserId);

        if (privateChatExists(creatorId, secondUserId)) {
            throw new IllegalArgumentException("Private chat already exists");
        }
        Set<Long> userIdsSet = new HashSet<>(Set.of(creatorId, secondUserId));
        ChatEntity chatEntity = createChat(chatCreateRequest, userIdsSet, creator);
        createChatUsers(chatEntity, userIdsSet);

        return ChatMapper.toDto(chatEntity);
    }

    private ChatDTO createGroupChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Set<Long> userIds = new HashSet<>(chatCreateRequest.getUserIds());
        userIds.add(creator.getId());

        ChatEntity chatEntity = createChat(chatCreateRequest, userIds, creator);
        createChatUsers(chatEntity, userIds);

        return ChatMapper.toDto(chatEntity);
    }

    private ChatDTO createSelfChat(ChatCreateRequest chatCreateRequest, UserEntity creator) {
        Long creatorId = creator.getId();

        if (getSelfChat(creatorId).isPresent()) {
            throw new IllegalArgumentException("Self chat already exists");
        }
        ChatEntity chatEntity = createChat(chatCreateRequest, new HashSet<>(Set.of(creatorId)), creator);
        createChatUsers(chatEntity, new HashSet<>(Set.of(creatorId)));
        return ChatMapper.toDto(chatEntity);
    }

    private ChatEntity createChat(ChatCreateRequest chatCreateRequest, Set<Long> userIds, UserEntity creator) {

        List<UserDTO> users = userServiceHelper.findUsersByIds(userIds);

        String chatName = users.stream()
                .map(UserDTO::getUsername)
                .collect(Collectors.joining(" "));

        if (chatName.length() > 100) {
            chatName = chatName.substring(0, 100);
        }

        ChatEntity chat = ChatEntity.builder()
                .name(chatName)
                .createdAt(LocalDateTime.now())
                .creator(creator)
                .type(chatCreateRequest.getChatType())
                .build();

        return chatRepository.save(chat);
    }

    private List<Long> createChatUsers(ChatEntity chat, Set<Long> userIds) {

        List<UserEntity> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new IllegalArgumentException("Some user IDs are invalid");
        }

        // Получаем уже существующих участников чата
        Set<Long> existingUserIds = chatUserRepository.findByChatId(chat.getId()).stream()
                .map(chatUser -> chatUser.getId().getUserId())
                .collect(Collectors.toSet());

        List<ChatUserEntity> newUsers = new ArrayList<>();
        for (UserEntity user : users) {
            if (!existingUserIds.contains(user.getId())) {
                newUsers.add(new ChatUserEntity(chat, user));
            }
        }

        String newUsersS = newUsers.stream()
                .map(chatUserEntity -> chatUserEntity.getId().getUserId())
                .toList()
                .toString();

        if (!newUsers.isEmpty()) {
            chatUserRepository.saveAll(newUsers); // Сохраняем всех за раз
            log.info("Added {} new users to chat {}", newUsersS, chat.getId());
        }

        return newUsers.stream()
                .map(chatUser -> chatUser.getId().getUserId())
                .toList();
    }

    private List<Long> deleteChatUsers(ChatEntity chat, Set<Long> userIds) {

        List<UserEntity> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new IllegalArgumentException("Some user IDs are invalid");
        }

        Set<Long> existingUserIds = chatUserRepository.findByChatId(chat.getId()).stream()
                .map(chatUser -> chatUser.getId().getUserId())
                .collect(Collectors.toSet());

        List<ChatUserEntity> delUsers = new ArrayList<>();
        for (UserEntity user : users) {
            if (existingUserIds.contains(user.getId())) {
                delUsers.add(new ChatUserEntity(chat, user));
            }
        }

        String delUsersS = delUsers.stream()
                .map(chatUserEntity -> chatUserEntity.getId().getUserId())
                .toList()
                .toString();
        if (!delUsers.isEmpty()) {
            chatUserRepository.deleteAll(delUsers);
            log.info("Deleted {} users from chat {}", delUsersS, chat.getId());
        }

        return delUsers.stream()
                .map(chatUser -> chatUser.getId().getUserId())
                .toList();
    }


    public boolean privateChatExists(Long userId1, Long userId2) {
        Optional<ChatEntity> chat = chatUserRepository.findPrivateChatBetweenUsers(userId1, userId2); // или Ordered, или JPQL версия
        return chat.isPresent();
    }

    public Optional<ChatEntity> getSelfChat(Long userId) {
        return chatUserRepository.findSelfChat(userId);
    }

    public ChatUserCreateResponse addUsersToChat(Long chatId, ChatUserCreateRequest chatUserCreateRequest, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }

        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException(String.format("Chat type %s is not supported adding users", chat.getType()));
        }

        List<Long> newUsers = createChatUsers(chat, new HashSet<>(chatUserCreateRequest.getUserIds()));

        ChatUserCreateResponse chatUserCreateResponse = ChatUserCreateResponse.builder()
                .chatId(chatId)
                .userIds(newUsers)
                .build();

        return chatUserCreateResponse;
    }

    public ChatUserDeleteResponse leaveUserFromChat(Long chatId, ChatUserDeleteRequest chatUserDeleteRequest, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (!chatUserService.userInChat(chatId, userDetails.getId())) {
            throw new AccessDeniedException("You are not in this chat");
        }

        if (!chatServiceHelper.userIsChatCreator(userDetails.getId(), chat.getCreator().getId())) {
            throw new AccessDeniedException("You are not creator of this chat");
        }

        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalArgumentException(String.format("Chat type %s is not supported deleting users", chat.getType()));
        }

        List<Long> newUsers = deleteChatUsers(chat, new HashSet<>(chatUserDeleteRequest.getUserIds()));

        ChatUserDeleteResponse chatUserDeleteResponse = ChatUserDeleteResponse.builder()
                .chatId(chatId)
                .userIds(newUsers)
                .build();

        return chatUserDeleteResponse;
    }

    @Transactional
    public void deleteChat(Long chatId, UserEntity userDetails) {
        ChatEntity chat = chatServiceHelper.findChatById(chatId);

        if (!chatServiceHelper.userIsChatCreator(userDetails.getId(), chat.getCreator().getId())) {
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
//        if (!chatServiceHelper.userIsChatCreator(user, chat)) {
//            throw new AccessDeniedException("Only creator can edit");
//        }

        log.debug(chatUpdateRequest.toString());
        chat.setName(chatUpdateRequest.getName());
        chat.setDescription(chatUpdateRequest.getDescription());

        ChatEntity updatedChat = chatRepository.save(chat);
        return ChatMapper.toDto(updatedChat);
    }



}
