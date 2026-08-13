package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisSessionService {
    private final StringRedisTemplate redisTemplate;

    private String key(UUID userId) {
        return "user:session:" + userId;
    }

    public void save(UUID userId, String sessionId , Duration expiration) {
        redisTemplate.opsForValue().set(key(userId), sessionId, expiration);
    }

    public String get(UUID userId) {
        return redisTemplate.opsForValue().get(key(userId));
    }

    public void delete(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    public boolean isValid(UUID userId, String sessionId) {
        String currentSessionId = get(userId);
        return currentSessionId != null && currentSessionId.equals(sessionId);
    }
}