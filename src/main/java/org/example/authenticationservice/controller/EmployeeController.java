package org.example.authenticationservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.JWTProperties;
import org.example.authenticationservice.dto.request.CreateEmployeeRequest;
import org.example.authenticationservice.dto.request.EmployeeLoginRequest;
import org.example.authenticationservice.dto.response.EmployeeResponse;
import org.example.authenticationservice.helpers.CookieHelper;
import org.example.authenticationservice.service.EmployeeService;
import org.example.authenticationservice.service.JWTService;
import org.example.authenticationservice.service.RedisSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final JWTService jwtService;
    private final RedisSessionService redisSessionService;
    private final JWTProperties jwtProperties;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(@Valid @RequestBody CreateEmployeeRequest request, Authentication authentication) {
        UUID organizationId = (UUID) authentication.getPrincipal();
        return employeeService.createEmployee(organizationId, request);
    }

    @PostMapping("/login")
    public EmployeeResponse login(@Valid @RequestBody EmployeeLoginRequest request, HttpServletResponse httpResponse) {
        EmployeeResponse employeeResponse = employeeService.login(request);
        String sessionId = UUID.randomUUID().toString();
        Duration refreshExpiration = Duration.ofMillis(jwtProperties.getRefreshExpiration());
        redisSessionService.save(employeeResponse.id(), sessionId, refreshExpiration);
        String accessToken = jwtService.generateAccessToken(employeeResponse.id(), employeeResponse.organizationId(), employeeResponse.role().name(), sessionId);
        String refreshToken = jwtService.generateRefreshToken(employeeResponse.id(), employeeResponse.organizationId(), employeeResponse.role().name(), sessionId);
        CookieHelper.addTokenCookie(httpResponse, "Access", accessToken, jwtProperties.getAccessExpiration());
        CookieHelper.addTokenCookie(httpResponse, "Refresh", refreshToken, jwtProperties.getRefreshExpiration());
        return employeeResponse;
    }
}