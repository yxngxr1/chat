package com.ssau.chat.repository;

import com.ssau.chat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByChat_Id(Long chatId);
}