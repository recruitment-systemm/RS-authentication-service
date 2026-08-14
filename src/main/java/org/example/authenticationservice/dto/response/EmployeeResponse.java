package org.example.authenticationservice.dto.response;

import lombok.Builder;
import org.example.authenticationservice.entity.EmployeeRole;

import java.util.UUID;

@Builder
public record EmployeeResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        EmployeeRole role,
        UUID organizationId
) {
}