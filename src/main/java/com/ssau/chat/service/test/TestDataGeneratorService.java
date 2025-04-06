package com.ssau.chat.service.test;

import com.ssau.chat.entity.*;
import com.ssau.chat.entity.enums.ChatType;
import com.ssau.chat.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestDataGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    private static final int USER_COUNT = 1000000;   // Количество пользователей
    private static final int CHAT_COUNT = 100000;    // Количество чатов
    private static final int MIN_USERS_PER_CHAT = 5;
    private static final int MAX_USERS_PER_CHAT = 10;
    private static final int MESSAGES_PER_CHAT_USER = 100;

    private static final int BATCH_SIZE = 100000;
    private static final String FIXED_TEXT_RU = "Время летит быстро, и каждый день приносит новые возможности для роста и самосовершенствования......";
    private static final String FIXED_TEXT_EN = "Time flies quickly, and each day brings new opportunities for growth and self-improvement...........";

    @Transactional
    public void generateTestData() {
        log.info("Generating test data...");

        List<Long> userIds = generateUsers();
        List<Long> chatsIds = generateChats(userIds);
        List<ChatUserId> chatUsers = generateChatUsers(chatsIds, userIds);

        generateMessages(chatUsers);
        log.info("Messages generated in count: {}", chatUsers.size() * MESSAGES_PER_CHAT_USER);
    }

    private List<Long> generateUsers() {
        log.info("Starting user generation for {} users", USER_COUNT);
        String sql = "INSERT INTO app_user (username, email, password, role, created_at) VALUES (?, ?, ?, ?, ?)";

        List<Object[]> users = new ArrayList<>(BATCH_SIZE);
        int totalCount = 0;
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        // Генерация пользователей
        for (int i = 0; i < USER_COUNT; i++) {
            users.add(new Object[]{
                    "user" + i,
                    "user" + i + "@example.com",
                    "password" + i,
                    Role.ROLE_USER.name(),
                    Timestamp.valueOf(now)
            });

            if (users.size() >= BATCH_SIZE) {
                totalCount = executeBatchInsert(sql, users, totalCount, startTime);
                users.clear();
            }
        }
        if (!users.isEmpty()) {
            executeBatchInsert(sql, users, totalCount, startTime);
        }

        String selectIdsSql = "SELECT id FROM app_user";
        List<Long> ids = jdbcTemplate.queryForList(selectIdsSql, Long.class);

        log.info("Users generated in count: {}", ids.size());
        return ids;
    }

    private List<Long> generateChats(List<Long> users) {
        log.info("Starting chat generation for {} chats", CHAT_COUNT);
        String sql = "INSERT INTO chat (name, description, creator_id, created_at, type) VALUES (?, ?, ?, ?, ?)";

        List<Object[]> chats = new ArrayList<>(BATCH_SIZE);
        int totalCount = 0;
        long startTime = System.currentTimeMillis();
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();

        // Генерация чатов
        for (int i = 0; i < CHAT_COUNT; i++) {
            // Выбираем случайного пользователя как создателя чата
            Long creator = users.get(random.nextInt(users.size()));

            // Добавляем чат в список для батч-вставки
            chats.add(new Object[]{
                    "Chat #" + i,
                    "Description for Chat #" + i,
                    creator,  // предполагаем, что в creator.getId() возвращается id пользователя
                    Timestamp.valueOf(now),
                    random.nextBoolean() ? ChatType.GROUP.name() : ChatType.PRIVATE.name()
            });

            // Если размер батча достигает BATCH_SIZE, выполняем вставку
            if (chats.size() >= BATCH_SIZE) {
                totalCount = executeBatchInsert(sql, chats, totalCount, startTime);
                chats.clear();
            }
        }

        // Выполняем вставку для оставшихся чатов
        if (!chats.isEmpty()) {
            executeBatchInsert(sql, chats, totalCount, startTime);
        }

        String selectIdsSql = "SELECT id FROM chat";
        List<Long> ids = jdbcTemplate.queryForList(selectIdsSql, Long.class);

        log.info("Chats generated in count: {}", ids.size());
        return ids;
    }

    private List<ChatUserId> generateChatUsers(List<Long> chats, List<Long> users) {
        log.info("Starting chat user generation for {} chats", chats.size() * ((MAX_USERS_PER_CHAT + MIN_USERS_PER_CHAT) / 2));
        String sql = "INSERT INTO chat_user (chat_id, user_id, joined_at) VALUES (?, ?, ?)";

        List<Object[]> chatUsers = new ArrayList<>(BATCH_SIZE);
        int totalCount = 0;
        long startTime = System.currentTimeMillis();
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();

        for (Long chatId : chats) {
            Set<Long> chatParticipants = new HashSet<>();
            int userCount = random.nextInt(MAX_USERS_PER_CHAT - MIN_USERS_PER_CHAT) + MIN_USERS_PER_CHAT;

            // Выбираем случайных пользователей для чата
            while (chatParticipants.size() < userCount) {
                chatParticipants.add(users.get(random.nextInt(users.size())));
            }

            // Добавляем в список для батч-вставки
            for (Long userId : chatParticipants) {
                chatUsers.add(new Object[]{
                        chatId,
                        userId,
                        Timestamp.valueOf(now.minusDays(ThreadLocalRandom.current().nextInt(30)))
                });

                // Если размер батча достигает BATCH_SIZE, выполняем вставку
                if (chatUsers.size() >= BATCH_SIZE) {
                    totalCount = executeBatchInsert(sql, chatUsers, totalCount, startTime);
                    chatUsers.clear();
                }
            }
        }

        // Выполняем вставку для оставшихся пользователей
        if (!chatUsers.isEmpty()) {
            executeBatchInsert(sql, chatUsers, totalCount, startTime);
        }


        String selectIdsSql = "SELECT chat_id, user_id FROM chat_user";

        List<ChatUserId> chatUserIds = jdbcTemplate.query(selectIdsSql, (rs, rowNum) -> {
            Long chatId = rs.getLong("chat_id");
            Long userId = rs.getLong("user_id");
            return new ChatUserId(chatId, userId);  // Создаем и возвращаем составной идентификатор
        });

        log.info("chat_user records generated in count: {}", chatUserIds.size());

        return chatUserIds;
    }



    public void generateMessages(List<ChatUserId> chatUsers) {
        log.info("Starting message generation for {} chat participants, Total messages: {}",
                chatUsers.size(), chatUsers.size() * MESSAGES_PER_CHAT_USER);
        String sql = "INSERT INTO message (chat_id, user_id, content, sent_at) VALUES (?, ?, ?, ?)";
        List<Object[]> messages = new ArrayList<>(BATCH_SIZE);
        int totalCount = 0;
        long startTime = System.currentTimeMillis();
        for (ChatUserId chatUser : chatUsers) {
            Long chatId = chatUser.getChatId();
            Long userId = chatUser.getUserId();

            for (int i = 0; i < MESSAGES_PER_CHAT_USER; i++) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime randomTime = now
                        .minusMonths(ThreadLocalRandom.current().nextInt(12))
                        .minusDays(ThreadLocalRandom.current().nextInt(30))
                        .minusHours(ThreadLocalRandom.current().nextInt(24))
                        .minusMinutes(ThreadLocalRandom.current().nextInt(60))
                        .minusSeconds(ThreadLocalRandom.current().nextInt(60));
                String fixedText = ThreadLocalRandom.current().nextBoolean() ? FIXED_TEXT_RU : FIXED_TEXT_EN;
                messages.add(new Object[]{
                        chatId,
                        userId,
                        fixedText,
                        Timestamp.valueOf(randomTime)
                });

                if (messages.size() >= BATCH_SIZE) {
                    totalCount = executeBatchInsert(sql, messages, totalCount, startTime);
                    messages.clear();
                }
            }
        }

        if (!messages.isEmpty()) {
            executeBatchInsert(sql, messages, totalCount, startTime);
        }
    }

    protected int executeBatchInsert(String sql, List<Object[]> batchArgs, int totalCount, long startTime) {
        jdbcTemplate.batchUpdate(sql, batchArgs);
        totalCount += batchArgs.size();
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Batch saved: {}, Total saved: {} (Elapsed time: {} s)",
                batchArgs.size(), totalCount, elapsedTime);
        return totalCount;
    }

    public void generateMessagesForChat(Long chatId, Long userId, int messageCount) {
        log.info("Starting message generation for user ID: {} in chat ID: {} with message count: {}",
                userId, chatId, messageCount);

        String sql = "INSERT INTO message (chat_id, user_id, content, sent_at) VALUES (?, ?, ?, ?)";
        List<Object[]> messages = new ArrayList<>(BATCH_SIZE);
        int totalCount = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i++) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime randomTime = now
                    .minusMonths(ThreadLocalRandom.current().nextInt(12))
                    .minusDays(ThreadLocalRandom.current().nextInt(30))
                    .minusHours(ThreadLocalRandom.current().nextInt(24))
                    .minusMinutes(ThreadLocalRandom.current().nextInt(60))
                    .minusSeconds(ThreadLocalRandom.current().nextInt(60));

            String fixedText = ThreadLocalRandom.current().nextBoolean() ? FIXED_TEXT_RU : FIXED_TEXT_EN;
            messages.add(new Object[]{
                    chatId,
                    userId,
                    fixedText,
                    Timestamp.valueOf(randomTime)
            });

            if (messages.size() >= BATCH_SIZE) {
                totalCount = executeBatchInsert(sql, messages, totalCount, startTime);
                messages.clear();
            }
        }

        // Если остались сообщения в батче, сохраняем их
        if (!messages.isEmpty()) {
            executeBatchInsert(sql, messages, totalCount, startTime);
        }
    }
}
