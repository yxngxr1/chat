package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "chat_user") // Название таблицы в БД
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserEntity {

    @EmbeddedId
    private ChatUserId id;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("chatId")
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("userId")
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChatUserEntity other = (ChatUserEntity) obj;
        return Objects.equals(id, other.id);
    }

}


