package com.example.auth_service.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final KeyPair keyPair;
    
    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;
    
    @Value("${jwt.issuer}")
    private String issuer;
    
    public String generateAccessToken(String username, Long userId, String[] roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("userId", userId);
        claims.put("roles", roles);
        claims.put("type", "ACCESS");
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }
    
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("userId", userId);
        claims.put("type", "REFRESH");
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }
    
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired");
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Long.class);
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}