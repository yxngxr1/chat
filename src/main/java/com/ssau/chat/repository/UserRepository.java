package com.ssau.chat.repository;

import com.ssau.chat.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}