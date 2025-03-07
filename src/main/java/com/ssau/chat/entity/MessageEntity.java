package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message") // Название таблицы в БД
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

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat; // Чат, к которому относится сообщение

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity sender; // Отправитель сообщения

    @Column(name = "content", nullable = false, length = 1000)
    private String content; // Текст сообщения

    @Column(name = "sent_at", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDateTime sentAt; // Время отправки

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}

