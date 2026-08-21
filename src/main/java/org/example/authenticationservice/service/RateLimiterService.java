package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.exceptions.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration BLOCK_DURATION = Duration.ofHours(24);

    private String attemptsKey(String action, String identifier) {
        return "rate-limit:%s:%s:%s".formatted(action, identifier, LocalDate.now());
    }

    private String blockKey(String action, String identifier) {
        return "rate-limit-blocked:%s:%s".formatted(action, identifier);
    }

    public void checkNotBlocked(String action, String identifier) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey(action, identifier)))) {
            throw new RateLimitExceededException("Too many attempts. Try again in 24 hours.");
        }
    }

    public void recordAttempt(String action, String identifier, int maxAttemptsPerDay) {
        String attemptsKey = attemptsKey(action, identifier);
        Long count = redisTemplate.opsForValue().increment(attemptsKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(attemptsKey, Duration.ofDays(1));
        }
        if (count != null && count > maxAttemptsPerDay) {
            redisTemplate.opsForValue().set(blockKey(action, identifier), "1", BLOCK_DURATION);
        }
    }
}
