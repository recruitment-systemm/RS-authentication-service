package org.example.authenticationservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.AdminProperties;
import org.example.authenticationservice.config.Properties.JWTProperties;
import org.example.authenticationservice.dto.request.AdminLoginRequest;
import org.example.authenticationservice.dto.request.OrganizationStatusUpdateRequest;
import org.example.authenticationservice.dto.response.ApiResponse;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.helpers.CookieHelper;
import org.example.authenticationservice.service.AdminService;
import org.example.authenticationservice.service.JWTService;
import org.example.authenticationservice.service.OrganizationService;
import org.example.authenticationservice.service.RedisSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminProperties adminProperties;
    private final JWTProperties jwtProperties;
    private final JWTService jwtService;
    private final RedisSessionService redisSessionService;
    private final OrganizationService organizationService;

    @PostMapping("/login")
    public ApiResponse<?> login( @Valid @RequestBody AdminLoginRequest request, HttpServletResponse httpResponse){
        adminService.login(request);
        UUID adminId = adminProperties.getId();
        String sessionId = UUID.randomUUID().toString();
        Duration refreshExpiration = Duration.ofMillis(jwtProperties.getRefreshExpiration());
        redisSessionService.save(adminId, sessionId, refreshExpiration);
        String accessToken = jwtService.generateAccessToken(adminId, sessionId);
        String refreshToken = jwtService.generateRefreshToken(adminId, sessionId);
        CookieHelper.addTokenCookie(httpResponse, "Access", accessToken, jwtProperties.getAccessExpiration());
        CookieHelper.addTokenCookie(httpResponse, "Refresh", refreshToken, jwtProperties.getRefreshExpiration());
        return ApiResponse.success(HttpStatus.OK.value(), "Admin login successful");
    }

    @PatchMapping("/organizations/{organizationId}/status")
    public ApiResponse<OrganizationResponse> updateOrganizationStatus(@PathVariable UUID organizationId, @Valid @RequestBody OrganizationStatusUpdateRequest request) {
        OrganizationResponse organizationResponse = organizationService.updateStatus(organizationId, request.status());
        return ApiResponse.success(HttpStatus.OK.value(), "Organization status updated successfully", organizationResponse);
    }
}
