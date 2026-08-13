package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private String key(UUID organizationId) {
        return "organization:profile:" + organizationId;
    }

    public void save(OrganizationResponse organization, Duration expiration) {
        try {
            String value = objectMapper.writeValueAsString(organization);
            redisTemplate.opsForValue().set(key(organization.id()), value, expiration);
        } catch (JacksonException exception) {
            throw new RuntimeException("Failed to cache organization profile", exception);
        }
    }

    public OrganizationResponse get(UUID organizationId) {
        try {
            String value = redisTemplate.opsForValue().get(key(organizationId));
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, OrganizationResponse.class);
        } catch (JacksonException exception) {
            throw new RuntimeException("Failed to read organization profile from cache", exception);
        }
    }

    public void delete(UUID organizationId) {
        redisTemplate.delete(key(organizationId));
    }
}