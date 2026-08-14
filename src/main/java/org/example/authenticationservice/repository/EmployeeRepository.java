package org.example.authenticationservice.repository;

import org.example.authenticationservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    boolean existsByUsernameAndOrganizationId(String username, UUID organizationId);
    boolean existsByLdapUid(String ldapUid);
    Optional<Employee> findByUsernameAndOrganizationId(String username, UUID organizationId);
    boolean existsByEmail(String email);
    boolean existsByOrganizationIdAndUsername(UUID organizationId, String username);
}