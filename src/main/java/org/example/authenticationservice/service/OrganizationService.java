package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.request.CreateOrganizationRequest;
import org.example.authenticationservice.dto.request.OrganizationLoginRequest;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.entity.Organization;
import org.example.authenticationservice.entity.OrganizationRole;
import org.example.authenticationservice.entity.OrganizationStatus;
import org.example.authenticationservice.exceptions.*;
import org.example.authenticationservice.repository.OrganizationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final OrganizationCacheService organizationCacheService;

    private OrganizationResponse mapToResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .email(organization.getEmail())
                .status(organization.getStatus())
                .requestedAt(organization.getRequestedAt())
                .taxRegistrationNumber(organization.getTaxRegistrationNumber())
                .taxRegistrationDocument(organization.getTaxRegistrationDocument())
                .build();
    }

    @Transactional
    public OrganizationResponse create(CreateOrganizationRequest request, MultipartFile document) {
        if (organizationRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (organizationRepository.existsByTaxRegistrationNumber(request.taxRegistrationNumber())) {
            throw new ResourceAlreadyExistsException("Tax registration number already exists");
        }
        String documentUrl = cloudinaryService.uploadDocument(document);
        Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
            .role(OrganizationRole.ORG)
                .status(OrganizationStatus.PENDING)
                .requestedAt(OffsetDateTime.now())
                .taxRegistrationNumber(request.taxRegistrationNumber())
                .taxRegistrationDocument(documentUrl)
                .build();
        Organization saved = organizationRepository.save(organization);
        return OrganizationResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .status(saved.getStatus())
                .requestedAt(saved.getRequestedAt())
                .taxRegistrationNumber(saved.getTaxRegistrationNumber())
                .taxRegistrationDocument(saved.getTaxRegistrationDocument())
                .build();
    }

    public OrganizationResponse login(OrganizationLoginRequest request){
        Organization organization = organizationRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), organization.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (organization.getRole() != OrganizationRole.ORG) {
            throw new ResourceNotFoundException("This account is not associated with an organization");
        }

        if(organization.getStatus() != OrganizationStatus.ACCEPTED){
            throw new OrganizationNotAcceptedException("Organization account has not been accepted yet");
        }

        return mapToResponse(organization);
    }

    public OrganizationResponse getProfile(UUID organizationId) {
        OrganizationResponse cachedOrganization = organizationCacheService.get(organizationId);
        if (cachedOrganization != null) {
            return cachedOrganization;
        }
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        OrganizationResponse organizationResponse = mapToResponse(organization);
        organizationCacheService.save(organizationResponse, Duration.ofMinutes(10));
        return organizationResponse;
    }

    public OrganizationResponse updateStatus(UUID organizationId, OrganizationStatus status) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (organization.getStatus() != OrganizationStatus.PENDING) {
            throw new InvalidOrganizationStatusException("Organization status has already been decided");
        }

        if (status != OrganizationStatus.ACCEPTED && status != OrganizationStatus.REJECTED) {
            throw new InvalidOrganizationStatusException("Organization status can only be ACCEPTED or REJECTED");
        }

        organization.setStatus(status);

        Organization savedOrganization = organizationRepository.save(organization);

        organizationCacheService.delete(organizationId);

        return OrganizationResponse.builder()
                .id(savedOrganization.getId())
                .name(savedOrganization.getName())
                .email(savedOrganization.getEmail())
                .status(savedOrganization.getStatus())
                .requestedAt(savedOrganization.getRequestedAt())
                .taxRegistrationNumber(savedOrganization.getTaxRegistrationNumber())
                .taxRegistrationDocument(savedOrganization.getTaxRegistrationDocument())
                .build();
    }
}