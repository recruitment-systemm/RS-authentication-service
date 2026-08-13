package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.request.CreateOrganizationRequest;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.entity.Organization;
import org.example.authenticationservice.entity.OrganizationStatus;
import org.example.authenticationservice.exceptions.ResourceAlreadyExistsException;
import org.example.authenticationservice.repository.OrganizationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

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
}