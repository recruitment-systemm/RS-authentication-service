package org.example.authenticationservice.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.AdminProperties;
import org.example.authenticationservice.dto.response.ErrorDetails;
import org.example.authenticationservice.dto.response.ErrorResponse;
import org.example.authenticationservice.service.JWTService;
import org.example.authenticationservice.service.RedisSessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final RedisSessionService redisSessionService;
    private final AdminProperties adminProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/v1/organizations")
                || path.equals("/api/v1/organizations/login")
                || path.equals("/api/v1/organizations/refresh")
                || path.equals("/api/v1/admin/login")
                || path.equals("/api/v1/organizations/linkedin")
                || path.equals("/api/v1/organizations/linkedin/callback")
                || path.equals("/api/v1/organizations/linkedin/signup/complete")
                || path.equals("/api/v1/employees/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!jwtService.isValid(token)) {
            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Invalid or expired access token"
            );
            return;
        }

        UUID userId = jwtService.extractUserId(token);
        String sessionId = jwtService.extractSessionId(token);

        if (!redisSessionService.isValid(userId, sessionId)) {
            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "SESSION_INVALID",
                    "Session is invalid or expired"
            );
            return;
        }

        if (isAdminRequest(request) && !adminProperties.getId().equals(userId)) {
            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN",
                    "You don't have permission to access this resource"
            );
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ErrorResponse errorResponse = new ErrorResponse(false, message, new ErrorDetails(code, null));
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/v1/admin/");
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "Access".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
