package org.example.authenticationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank String token,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%!]).{8,}$",
                message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character"
        )
        String newPassword
) {}