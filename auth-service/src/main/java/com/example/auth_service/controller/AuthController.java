package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.EmailSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final EmailSyncService emailSyncService;
    
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("🔐 Registrando usuario: {}", request.getUsername());
        
        JwtResponse response = authService.register(request);
        
        // Sincronizar usuario (con cédula y dirección) con payment-service
        try {
            Long userId = response.getUser().getId();
            User user = new User();
            user.setId(userId);
            user.setEmail(response.getUser().getEmail());
            user.setCedula(response.getUser().getCedula());
            user.setDireccion(response.getUser().getDireccion());
            
            emailSyncService.sincronizarUsuarioConPaymentService(String.valueOf(userId), user);
            log.info("✅ Usuario sincronizado con payment-service: {} (Email, Cédula, Dirección)", userId);
        } catch (Exception e) {
            log.warn("⚠️ Error sincronizando usuario con payment-service: {}", e.getMessage());
            // No fallamos el registro si falla la sincronización
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        JwtResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}