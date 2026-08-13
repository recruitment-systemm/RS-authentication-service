package org.example.authenticationservice.dto.response;

import lombok.Builder;
import org.example.authenticationservice.entity.OrganizationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record OrganizationResponse(UUID id, String name, String email, OrganizationStatus status, OffsetDateTime requestedAt, String taxRegistrationNumber, String taxRegistrationDocument) { }