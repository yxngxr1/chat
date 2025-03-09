package com.ssau.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "app_user") // Название таблицы в БД
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password; // Хешированный пароль

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDateTime createdAt; // Дата регистрации

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatUserEntity> chatUser; // Список чатов, в которых состоит пользователь

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

