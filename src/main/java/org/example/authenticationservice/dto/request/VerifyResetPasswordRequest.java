package org.example.authenticationservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyResetPasswordRequest(@NotBlank String token) { }
