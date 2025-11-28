package com.example.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Error setting value in Redis for key: {}", key, e);
        }
    }
    
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting value in Redis for key: {}", key, e);
        }
    }
    
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting value from Redis for key: {}", key, e);
            return null;
        }
    }
    
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting key from Redis: {}", key, e);
            return false;
        }
    }
    
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting key from Redis: {}", key, e);
        }
    }
    
    public void addToBlacklist(String token, long expirationSeconds) {
        try {
            String key = "blacklist:" + token;
            redisTemplate.opsForValue().set(key, "revoked", expirationSeconds, TimeUnit.SECONDS);
            log.info("Token added to blacklist");
        } catch (Exception e) {
            log.error("Error adding token to blacklist", e);
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = "blacklist:" + token;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Error checking if token is blacklisted", e);
            return false;
        }
    }
    
    public void updateUserActivity(Long userId) {
        try {
            String key = "user_activity:" + userId;
            redisTemplate.opsForValue().set(key, System.currentTimeMillis(), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Error updating user activity", e);
        }
    }
}