package org.example.authenticationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private OrganizationStatus status;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "tax_registration_number", nullable = false, unique = true)
    private String taxRegistrationNumber;

    @Column(name = "tax_registration_document", nullable = false)
    private String taxRegistrationDocument;
}