package com.example.auth_service.repository;

import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
    List<RefreshToken> findByExpiryDateBefore(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
    void deleteAllExpiredSince(LocalDateTime now);
}