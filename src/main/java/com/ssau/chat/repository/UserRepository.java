package com.ssau.chat.repository;

import com.ssau.chat.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.id IN :userIds")
    List<UserEntity> findUsernamesByIds(@Param("userIds") Set<Long> userIds);

    @Query(value = "SELECT * FROM app_user WHERE username ILIKE %:query% LIMIT 10", nativeQuery = true)
    List<UserEntity> searchByUsername(@Param("query") String query);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}