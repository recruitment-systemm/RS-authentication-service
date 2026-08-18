package org.example.authenticationservice.helpers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieHelper {

    @Value("${app.cookie.cross-site:false}")
    private boolean crossSite;

    private static boolean staticCrossSite = false;

    @jakarta.annotation.PostConstruct
    private void init() {
        staticCrossSite = crossSite;
    }

    public static void addTokenCookie(HttpServletResponse response, String name, String token, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(staticCrossSite)
                .path("/")
                .maxAge(maxAge)
                .sameSite(staticCrossSite ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(staticCrossSite)
                .path("/")
                .maxAge(0)
                .sameSite(staticCrossSite ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}