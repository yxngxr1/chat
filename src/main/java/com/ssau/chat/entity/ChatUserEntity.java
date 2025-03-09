package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_user") // Название таблицы в БД
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserEntity {

    @EmbeddedId
    private ChatUserId id = new ChatUserId(); // Составной ключ

    @ManyToOne
    @MapsId("chatId") // Привязываем поле chatId из ChatUserId к сущности ChatEntity
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @ManyToOne
    @MapsId("userId") // Привязываем поле userId из ChatUserId к сущности UserEntity
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "joined_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    public ChatUserEntity(ChatEntity chat, UserEntity user) {
        this.id = new ChatUserId(chat.getId(), user.getId());
        this.chat = chat;
        this.user = user;
    }
}


