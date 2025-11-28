package com.example.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // <- AGREGAR ESTO
public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;
}