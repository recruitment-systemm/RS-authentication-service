package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.LinkedInSignupData;
import org.example.authenticationservice.dto.request.CompleteLinkedInSignupRequest;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.entity.AuthProvider;
import org.example.authenticationservice.entity.Organization;
import org.example.authenticationservice.entity.OrganizationRole;
import org.example.authenticationservice.entity.OrganizationStatus;
import org.example.authenticationservice.exceptions.InvalidLinkedInSignupTokenException;
import org.example.authenticationservice.exceptions.ResourceAlreadyExistsException;
import org.example.authenticationservice.repository.OrganizationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkedInSignupCompletionService {

    private final LinkedInSignupService linkedInSignupService;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public OrganizationResponse complete(CompleteLinkedInSignupRequest request) {
        LinkedInSignupData linkedinData = linkedInSignupService.get(request.token());
        if (linkedinData == null) {
            throw new InvalidLinkedInSignupTokenException("LinkedIn signup token is invalid or expired");
        }

        if (organizationRepository.existsByEmail(linkedinData.email())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (organizationRepository.existsByTaxRegistrationNumber(request.taxRegistrationNumber())) {
            throw new ResourceAlreadyExistsException("Tax registration number already exists");
        }

        String documentUrl = cloudinaryService.uploadDocument(request.taxRegistrationDocument());

        Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(request.organizationName())
                .email(linkedinData.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(OrganizationRole.ORG)
                .status(OrganizationStatus.PENDING)
                .requestedAt(OffsetDateTime.now())
                .taxRegistrationNumber(request.taxRegistrationNumber())
                .taxRegistrationDocument(documentUrl)
                .linkedinId(linkedinData.linkedinId())
                .authProvider(AuthProvider.LINKEDIN)
                .build();

        Organization saved = organizationRepository.save(organization);
        linkedInSignupService.delete(request.token());

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
}
