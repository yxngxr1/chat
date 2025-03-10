package com.ssau.chat.repository;

import com.ssau.chat.entity.MessageEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByChat_Id(Long chatId);

    @Transactional
    @Modifying
    @Query("DELETE FROM MessageEntity m WHERE m.chat.id = :chatId")
    int deleteAllByChatId(Long chatId);
}