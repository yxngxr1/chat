package com.ssau.chat.repository;

import com.ssau.chat.entity.ChatEntity;
import com.ssau.chat.entity.ChatUserEntity;
import com.ssau.chat.entity.ChatUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUserEntity, ChatUserId> {
    @Query(value = "SELECT c.* FROM chat c " +
            "JOIN chat_user cu1 ON c.id = cu1.chat_id " +
            "JOIN chat_user cu2 ON c.id = cu2.chat_id " +
            "WHERE c.type = 'PRIVATE' " +
            "AND cu1.user_id = :userId1 " +
            "AND cu2.user_id = :userId2 " +
            "AND cu1.user_id <> cu2.user_id", nativeQuery = true)
    Optional<ChatEntity> findPrivateChatBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query(value = "SELECT c.* FROM chat c " +
            "JOIN chat_user cu ON c.id = cu.chat_id " +
            "WHERE c.type = 'SELF' " +
            "AND cu.user_id = :userId1", nativeQuery = true)
    Optional<ChatEntity> findSelfChat(@Param("userId1") Long userId);
}