package com.example.auth_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String cedula;
    private String direccion;
    private Set<String> roles;
    private LocalDateTime createdAt;
}