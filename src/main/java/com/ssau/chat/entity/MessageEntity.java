package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message",
    indexes = {
        @Index(name = "idx_message_chat_id_sent_at", columnList = "chat_id, sent_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat; // Чат, к которому относится сообщение

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity sender; // Отправитель сообщения

    @Column(name = "content", nullable = false, length = 10000) // от 1 до 40000 байт
    private String content; // Текст сообщения

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}

