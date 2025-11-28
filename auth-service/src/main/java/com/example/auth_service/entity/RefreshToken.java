package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data // <- AGREGAR ESTO
@NoArgsConstructor
@AllArgsConstructor
@Builder // <- AGREGAR ESTO TAMBIÉN
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "revoked")
    private boolean revoked = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}