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

    private String linkedInKey(String state) {
        return "oauth:linkedin:" + state;
    }

    public void save(UUID userId, String sessionId, Duration expiration) {
        redisTemplate.opsForValue().set(key(userId), sessionId, expiration);
    }

    public String get(UUID userId) {
        return redisTemplate.opsForValue().get(key(userId));
    }

    public void delete(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    public void deleteAllSessions(UUID organizationId) {
        delete(organizationId);
    }

    public boolean isValid(UUID userId, String sessionId) {
        String currentSessionId = get(userId);
        return currentSessionId != null && currentSessionId.equals(sessionId);
    }

    public void saveLinkedInData(
            String state,
            String linkedinId,
            String name,
            String email,
            String givenName,
            String familyName,
            String picture,
            Duration expiration
    ) {
        String key = linkedInKey(state);

        redisTemplate.opsForHash().put(key, "linkedinId", linkedinId);
        redisTemplate.opsForHash().put(key, "name", name);
        redisTemplate.opsForHash().put(key, "email", email);
        redisTemplate.opsForHash().put(key, "givenName", givenName);
        redisTemplate.opsForHash().put(key, "familyName", familyName);
        redisTemplate.opsForHash().put(key, "picture", picture);

        redisTemplate.expire(key, expiration);
    }

    public void saveLinkedInState(String state, Duration expiration) {
        redisTemplate.opsForValue().set("oauth:linkedin:" + state, "PENDING", expiration);
    }

    public String getLinkedInData(String state, String field) {
        Object value = redisTemplate.opsForHash().get(linkedInKey(state), field);
        return value != null ? value.toString() : null;
    }

    public boolean hasLinkedInData(String state) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(linkedInKey(state)));
    }

    public void deleteLinkedInData(String state) {
        redisTemplate.delete(linkedInKey(state));
    }
}