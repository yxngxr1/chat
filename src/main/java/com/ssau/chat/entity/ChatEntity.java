package com.ssau.chat.entity;

import com.ssau.chat.entity.enums.ChatType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "chat") // Название таблицы в БД
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Название чата

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDateTime createdAt; // Дата создания чата

    @Enumerated(EnumType.STRING) // Хранить enum как строку (имена)
    @Column(name = "type", nullable = false)
    private ChatType type;  // Добавлено поле для типа чата

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatUserEntity> chatUser; // Участники чата

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

