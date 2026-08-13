package org.example.authenticationservice.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.JWTProperties;
import org.example.authenticationservice.dto.request.CreateOrganizationRequest;
import org.example.authenticationservice.dto.request.OrganizationLoginRequest;
import org.example.authenticationservice.dto.response.ApiResponse;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.helpers.CookieHelper;
import org.example.authenticationservice.service.JWTService;
import org.example.authenticationservice.service.OrganizationService;
import org.example.authenticationservice.service.RedisSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Service
public class OrganizationController {
    private final OrganizationService organizationService;
    private final JWTService jwtService;
    private final RedisSessionService redisSessionService;
    private final JWTProperties jwtProperties;

    private String extractAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "Access".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "Refresh".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrganizationResponse> create(@Valid @ModelAttribute CreateOrganizationRequest request, @RequestParam("taxRegistrationDocument") MultipartFile taxRegistrationDocument) {
        OrganizationResponse response = organizationService.create(request, taxRegistrationDocument);
        return ApiResponse.success(HttpStatus.CREATED.value(), "Organization created successfully", response);
    }

    @PostMapping("/login")
    public ApiResponse<OrganizationResponse> login(@Valid @RequestBody OrganizationLoginRequest request, HttpServletResponse httpResponse) {
        OrganizationResponse organizationResponse = organizationService.login(request);
        String sessionId = UUID.randomUUID().toString();
        Duration refreshExpiration = Duration.ofMillis(jwtProperties.getRefreshExpiration());
        redisSessionService.save(organizationResponse.id(), sessionId , refreshExpiration);
        String accessToken = jwtService.generateAccessToken(organizationResponse.id(), sessionId);
        String refreshToken = jwtService.generateRefreshToken(organizationResponse.id(), sessionId);
        CookieHelper.addTokenCookie(httpResponse, "Access", accessToken, jwtProperties.getAccessExpiration());
        CookieHelper.addTokenCookie(httpResponse, "Refresh", refreshToken, jwtProperties.getRefreshExpiration());
        return ApiResponse.success(HttpStatus.OK.value(), "Login successful", organizationResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Claims claims = jwtService.extractClaims(refreshToken);
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UUID userId = UUID.fromString(claims.getSubject());
            String sessionId = claims.get("sessionId", String.class);
            if (!redisSessionService.isValid(userId, sessionId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String accessToken = jwtService.generateAccessToken(userId, sessionId);
            CookieHelper.addTokenCookie(response, "Access", accessToken, jwtProperties.getAccessExpiration());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        if (accessToken != null) {
            try {
                Claims claims = jwtService.extractClaims(accessToken);
                String tokenType = claims.get("type", String.class);
                if ("access".equals(tokenType)) {
                    UUID userId = UUID.fromString(claims.getSubject());
                    String sessionId = claims.get("sessionId", String.class);
                    if (redisSessionService.isValid(userId, sessionId)) {
                        redisSessionService.delete(userId);
                    }
                }

            } catch (Exception ignored) {}
        }
        CookieHelper.deleteCookie(response, "Access");
        CookieHelper.deleteCookie(response, "Refresh");
        return ApiResponse.success(HttpStatus.OK.value(), "Logout successful");
    }

    @GetMapping("/profile")
    public ApiResponse<OrganizationResponse> profile(Authentication authentication) {
        UUID organizationId = (UUID) authentication.getPrincipal();
        OrganizationResponse organizationResponse = organizationService.getProfile(organizationId);
        return ApiResponse.success(HttpStatus.OK.value(), "Profile retrieved successfully", organizationResponse);
    }
}