package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message", indexes = {
        @Index(name = "idx_chat_id", columnList = "chat_id") // Индекс на chat_id
})
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

    @Column(name = "content", nullable = false, length = 10000)
    private String content; // Текст сообщения

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}

