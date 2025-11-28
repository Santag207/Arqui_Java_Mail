package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long expiresIn;
    private UserResponse user;
}