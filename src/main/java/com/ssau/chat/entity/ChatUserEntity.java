package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_user") // Название таблицы в БД
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserEntity {

    @EmbeddedId
    private ChatUserId id; // Составной ключ (chat_id, user_id)

    @ManyToOne
    @MapsId("chatId") // Связь с полем chatId из составного ключа
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @ManyToOne
    @MapsId("userId") // Связь с полем userId из составного ключа
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "joined_at", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}


