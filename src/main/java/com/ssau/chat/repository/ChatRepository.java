package com.ssau.chat.repository;

import com.ssau.chat.entity.ChatEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM ChatEntity c WHERE c.id = :chatId")
    void deleteById(Long chatId);
}
