package org.example.authenticationservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%!]).{8,}$",
                message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character"
        )
        String password,

        @NotBlank(message = "Tax registration number is required")
        @Pattern(
                regexp = "^EG-[0-9]{3}-[0-9]{3}-[0-9]{3}$",
                message = "Invalid tax registration number"
        )
        String taxRegistrationNumber
) { }