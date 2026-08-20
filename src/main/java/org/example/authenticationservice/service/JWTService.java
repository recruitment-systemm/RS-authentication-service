package org.example.authenticationservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.authenticationservice.config.Properties.JWTProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JWTService {

    private final JWTProperties jwtProperties;
    private final SecretKey key;

    public JWTService(JWTProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(UUID userId, String sessionId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("sessionId", sessionId)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(UUID userId, UUID organizationId, String role, String sessionId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("organizationId", organizationId.toString())
                .claim("role", role)
                .claim("sessionId", sessionId)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateAdminAccessToken(UUID adminId, String sessionId) {
        return Jwts.builder()
                .subject(adminId.toString())
                .claim("role", "ADMIN")
                .claim("sessionId", sessionId)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateAdminRefreshToken(UUID adminId, String sessionId) {
        return Jwts.builder()
                .subject(adminId.toString())
                .claim("role", "ADMIN")
                .claim("sessionId", sessionId)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String sessionId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("sessionId", sessionId)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID userId, UUID organizationId, String role, String sessionId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("organizationId", organizationId.toString())
                .claim("role", role)
                .claim("sessionId", sessionId)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public String extractSessionId(String token) {
        return extractClaims(token).get("sessionId", String.class);
    }

    public String extractTokenType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    public UUID extractOrganizationId(String token) {
        String organizationId = extractClaims(token).get("organizationId", String.class);
        return organizationId != null ? UUID.fromString(organizationId) : null;
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}