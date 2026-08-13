package org.example.authenticationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.authenticationservice.entity.OrganizationStatus;

public record OrganizationStatusUpdateRequest(@NotNull OrganizationStatus status) { }