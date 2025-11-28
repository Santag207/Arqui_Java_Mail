package com.tours.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    private PublicKey getPublicKey() {
        try {
            ClassPathResource resource = new ClassPathResource(publicKeyPath);
            byte[] keyBytes = resource.getInputStream().readAllBytes();
            String publicKeyPEM = new String(keyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Error loading public key from: {}", publicKeyPath, e);
            throw new RuntimeException("Error loading public key", e);
        }
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token: " + e.getMessage());
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

    public String[] getRolesFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("roles", String[].class);
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
        } catch (Exception e) {
            log.error("Error checking token blacklist", e);
            return false;
        }
    }

    public boolean validateTokenStructure(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}