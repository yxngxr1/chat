package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "chat") // Название таблицы в БД
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Название чата

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDateTime createdAt; // Дата создания чата

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatUserEntity> chatUser; // Участники чата

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

