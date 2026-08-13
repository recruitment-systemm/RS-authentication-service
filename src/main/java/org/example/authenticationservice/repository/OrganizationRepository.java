package org.example.authenticationservice.repository;

import org.example.authenticationservice.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByEmail(String email);
    boolean existsByTaxRegistrationNumber(String taxRegistrationNumber);
}