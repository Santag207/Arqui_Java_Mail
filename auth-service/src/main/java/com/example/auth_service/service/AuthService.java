package com.example.auth_service.service;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        User user = userService.createUser(
            request.getUsername(), 
            request.getEmail(), 
            request.getPassword(), 
            request.getRole() != null ? request.getRole() : "USER",
            request.getCedula(),
            request.getDireccion()
        );
        
        return generateJwtResponse(user);
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        User user = userService.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Update user activity in Redis
        redisService.updateUserActivity(user.getId());

        return generateJwtResponse(user);
    }

    @Transactional
    public JwtResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        RefreshToken storedToken = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        storedToken = refreshTokenService.verifyExpiration(storedToken);
        User user = storedToken.getUser();

        // Update user activity
        redisService.updateUserActivity(user.getId());

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(
            user.getUsername(), 
            user.getId(), 
            user.getRoles().toArray(new String[0])
        );
        
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getId());

        // Revoke old refresh token and create new one
        refreshTokenService.revokeToken(storedToken);
        RefreshToken newStoredToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(
            newAccessToken, 
            newStoredToken.getToken(), 
            "Bearer", 
            jwtService.getAccessTokenExpiration(),
            convertToUserResponse(user)
        );
    }

    @Transactional
    public void logout(String accessToken) {
        try {
            // Add token to blacklist
            long expiration = jwtService.getAccessTokenExpiration();
            redisService.addToBlacklist(accessToken, expiration);

            // Get user from token and revoke refresh tokens
            String username = jwtService.getUsernameFromToken(accessToken);
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            refreshTokenService.revokeAllUserTokens(user);

            // Remove user activity
            redisService.deleteKey("user_activity:" + user.getId());

            log.info("User {} logged out successfully", username);
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new RuntimeException("Logout failed");
        }
    }

    public boolean validateToken(String token) {
        try {
            return !jwtService.isTokenExpired(token) && !redisService.isTokenBlacklisted(token);
        } catch (Exception e) {
            log.error("Token validation error", e);
            return false;
        }
    }

    private JwtResponse generateJwtResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
            user.getUsername(), 
            user.getId(), 
            user.getRoles().toArray(new String[0])
        );
        
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getId());
        RefreshToken storedRefreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(
            accessToken,
            storedRefreshToken.getToken(),
            "Bearer",
            jwtService.getAccessTokenExpiration(),
            convertToUserResponse(user)
        );
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setRoles(user.getRoles());
        userResponse.setCreatedAt(user.getCreatedAt());
        return userResponse;
    }
}