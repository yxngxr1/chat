package com.ssau.chat.repository;

import com.ssau.chat.entity.MessageEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("SELECT m FROM MessageEntity m WHERE m.chat.id = :chatId ORDER BY m.sentAt DESC")
    List<MessageEntity> findByChatId(Long chatId);

    @Query(value = "SELECT * FROM message m WHERE m.chat_id = :chatId AND m.sent_at < :lastSentAt ORDER BY m.sent_at DESC LIMIT :limit", nativeQuery = true)
    List<MessageEntity> findMessagesUMOM(
            @Param("chatId") Long chatId,
            @Param("lastSentAt") LocalDateTime lastSentAt,
            @Param("limit") int limit);
}