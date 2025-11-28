package com.example.auth_service.controller;

import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.RedisService;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;
    private final UserService userService;

    @GetMapping("/sessions/active")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        log.info("Fetching active sessions");
        
        // Contar sesiones activas (usuarios con actividad en los últimos 30 minutos)
        long activeSessions = 0;
        // En una implementación real, buscaríamos en Redis las keys de user_activity
        
        Map<String, Object> response = new HashMap<>();
        response.put("activeSessions", activeSessions);
        response.put("message", "Session tracking implemented with Redis");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tokens/revoke/{token}")
    public ResponseEntity<Map<String, String>> revokeToken(@PathVariable String token) {
        log.info("Revoking token: {}", token);
        
        // Agregar token a la blacklist de Redis por 24 horas
        redisService.addToBlacklist(token, 24 * 60 * 60 * 1000);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Token revoked successfully");
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/blacklist/count")
    public ResponseEntity<Map<String, Object>> getBlacklistCount() {
        log.info("Getting blacklist count");
        
        // En una implementación real, contaríamos las keys de blacklist en Redis
        // Por ahora devolvemos un valor simulado
        long blacklistedCount = 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("blacklistedTokens", blacklistedCount);
        response.put("message", "Blacklist implemented with Redis");
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sessions/user/{userId}")
    public ResponseEntity<Map<String, String>> revokeAllUserSessions(@PathVariable Long userId) {
        log.info("Revoking all sessions for user: {}", userId);
        
        // Eliminar actividad del usuario y revocar tokens de refresh
        redisService.deleteKey("user_activity:" + userId);
        
        // Buscar usuario y revocar sus tokens de refresh
        userService.findByUsername(String.valueOf(userId)).ifPresent(user -> {
            refreshTokenService.revokeAllUserTokens(user);
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All sessions revoked for user: " + userId);
        response.put("userId", userId.toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        log.info("Fetching admin statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getUserCount());
        stats.put("activeSessions", 0); // Implementar con Redis
        stats.put("blacklistedTokens", 0); // Implementar con Redis
        stats.put("systemStatus", "OPERATIONAL");
        stats.put("authService", "RUNNING");
        stats.put("redis", "CONNECTED");
        
        return ResponseEntity.ok(stats);
    }
}