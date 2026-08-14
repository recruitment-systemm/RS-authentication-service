package org.example.authenticationservice.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public record CompleteLinkedInSignupRequest(

        @NotBlank
        String token,

        @NotBlank
        String organizationName,

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
        String taxRegistrationNumber,

        @NotNull(message = "Tax registration document is required")
        MultipartFile taxRegistrationDocument

) {
        @AssertTrue(message = "Tax registration document is required")
        public boolean isTaxRegistrationDocumentProvided() {
                return taxRegistrationDocument != null && !taxRegistrationDocument.isEmpty();
        }
}
