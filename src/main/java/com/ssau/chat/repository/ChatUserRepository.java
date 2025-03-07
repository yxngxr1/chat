package com.ssau.chat.repository;

import com.ssau.chat.entity.ChatUserEntity;
import com.ssau.chat.entity.ChatUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatUserRepository extends JpaRepository<ChatUserEntity, ChatUserId> {
}