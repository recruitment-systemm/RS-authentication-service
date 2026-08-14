package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.AdminProperties;
import org.example.authenticationservice.dto.request.AdminLoginRequest;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.entity.Organization;
import org.example.authenticationservice.exceptions.InvalidCredentialsException;
import org.example.authenticationservice.exceptions.ResourceNotFoundException;
import org.example.authenticationservice.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminProperties adminProperties;
    private final OrganizationRepository organizationRepository;

    public void login(AdminLoginRequest request) {
        if (!adminProperties.getEmail().equals(request.email())
                || !adminProperties.getPassword().equals(request.password())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrganizationResponse getOrganizationById(UUID organizationId) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return mapToResponse(organization);
    }

    private OrganizationResponse mapToResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getEmail(),
                organization.getStatus(),
                organization.getRequestedAt(),
                organization.getTaxRegistrationNumber(),
                organization.getTaxRegistrationDocument()
        );
    }
}