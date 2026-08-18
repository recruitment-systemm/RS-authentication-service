package org.example.authenticationservice.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.JWTProperties;
import org.example.authenticationservice.dto.request.CompleteLinkedInSignupRequest;
import org.example.authenticationservice.dto.LinkedInSignupData;
import org.example.authenticationservice.dto.request.*;
import org.example.authenticationservice.dto.response.ApiResponse;
import org.example.authenticationservice.dto.response.LinkedInTokenResponse;
import org.example.authenticationservice.dto.response.LinkedInUserInfoResponse;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.helpers.CookieHelper;
import org.example.authenticationservice.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final JWTService jwtService;
    private final RedisSessionService redisSessionService;
    private final JWTProperties jwtProperties;
    private final LinkedInOAuthService linkedInOAuthService;
    private final LinkedInSignupService linkedInSignupService;
    private final LinkedInSignupCompletionService linkedInSignupCompletionService;

    @Value("${frontend.url}")
    private String frontendUrl;

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

            String organizationIdClaim = claims.get("organizationId", String.class);
            String role = claims.get("role", String.class);
            String accessToken = (organizationIdClaim != null && role != null)
                    ? jwtService.generateAccessToken(userId, UUID.fromString(organizationIdClaim), role, sessionId)
                    : jwtService.generateAccessToken(userId, sessionId);
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

    @GetMapping("/lookup")
    public ApiResponse<org.example.authenticationservice.dto.response.OrganizationLookupResponse> lookup(@RequestParam String employeeEmail) {
        UUID organizationId = organizationService.findOrganizationIdByEmployeeEmail(employeeEmail);
        return ApiResponse.success(HttpStatus.OK.value(), "Organization found", new org.example.authenticationservice.dto.response.OrganizationLookupResponse(organizationId));
    }

    @GetMapping("/profile")
    public ApiResponse<OrganizationResponse> profile(Authentication authentication) {
        UUID organizationId = (UUID) authentication.getPrincipal();
        OrganizationResponse organizationResponse = organizationService.getProfile(organizationId);
        return ApiResponse.success(HttpStatus.OK.value(), "Profile retrieved successfully", organizationResponse);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        organizationService.forgotPassword(request.email());
        return ApiResponse.success(HttpStatus.OK.value(), "If an account exists with this email, a password reset link has been sent");
    }

    @PostMapping("/reset-password/verify")
    public ApiResponse<Void> verifyResetPassword(@Valid @RequestBody VerifyResetPasswordRequest request) {
        organizationService.verifyPasswordResetToken(request.token());
        return ApiResponse.success(HttpStatus.OK.value(), "Password reset token is valid");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        organizationService.resetPassword(request.token(), request.newPassword());
        return ApiResponse.success(HttpStatus.OK.value(), "Password has been reset successfully");
    }

    @GetMapping("/linkedin")
    public void linkedinLogin(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        redisSessionService.saveLinkedInState(state, Duration.ofMinutes(10));
        String authorizationUrl = linkedInOAuthService.buildAuthorizationUrl(state);
        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/linkedin/callback")
    public void linkedinCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        LinkedInTokenResponse tokenResponse = linkedInOAuthService.exchangeCodeForToken(code);
        LinkedInUserInfoResponse userInfo = linkedInOAuthService.getUserInfo(tokenResponse.accessToken());
        String signupToken = linkedInSignupService.save(
                new LinkedInSignupData(
                        userInfo.sub(),
                        userInfo.name(),
                        userInfo.email(),
                        userInfo.givenName(),
                        userInfo.familyName(),
                        userInfo.picture()
                ),
                Duration.ofMinutes(10)
        );
        response.sendRedirect(frontendUrl + "/signup/linkedin?token=" + signupToken);
    }

    @PostMapping(value = "/linkedin/signup/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrganizationResponse> completeLinkedInSignup(@Valid @ModelAttribute CompleteLinkedInSignupRequest request) {
        OrganizationResponse response = linkedInSignupCompletionService.complete(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), "LinkedIn organization signup completed successfully", response);
    }
}
