package org.example.authenticationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.LinkedInSignupData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkedInSignupService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String PREFIX = "linkedin:signup:";

    public String save(LinkedInSignupData data, Duration expiration) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, data, expiration);
        return token;
    }

    public LinkedInSignupData get(String token) {
        Object value = redisTemplate.opsForValue().get(PREFIX + token);
        if (value == null) {
            return null;
        }
        if (value instanceof LinkedInSignupData linkedInSignupData) {
            return linkedInSignupData;
        }
        return objectMapper.convertValue(value, LinkedInSignupData.class);
    }

    public void delete(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
