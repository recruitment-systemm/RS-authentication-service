package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final StringRedisTemplate redisTemplate;

    private String key(String token) {
        return "password-reset:" + token;
    }

    public String createToken(UUID organizationId, Duration expiration) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key(token), organizationId.toString(), expiration);
        return token;
    }

    public UUID getOrganizationId(String token) {
        String value = redisTemplate.opsForValue().get(key(token));
        return value == null ? null : UUID.fromString(value);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(key(token));
    }
}