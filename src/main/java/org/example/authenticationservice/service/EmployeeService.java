package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.request.CreateEmployeeRequest;
import org.example.authenticationservice.dto.request.EmployeeLoginRequest;
import org.example.authenticationservice.dto.response.EmployeeResponse;
import org.example.authenticationservice.entity.Employee;
import org.example.authenticationservice.entity.Organization;
import org.example.authenticationservice.entity.OrganizationRole;
import org.example.authenticationservice.entity.OrganizationStatus;
import org.example.authenticationservice.exceptions.InvalidCredentialsException;
import org.example.authenticationservice.exceptions.InvalidEmployeeEmailException;
import org.example.authenticationservice.exceptions.OrganizationNotAcceptedException;
import org.example.authenticationservice.exceptions.ResourceAlreadyExistsException;
import org.example.authenticationservice.exceptions.ResourceNotFoundException;
import org.example.authenticationservice.repository.EmployeeRepository;
import org.example.authenticationservice.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final LdapUserService ldapUserService;

    @Transactional
    public EmployeeResponse createEmployee(UUID organizationId, CreateEmployeeRequest request) {

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (organization.getRole() != OrganizationRole.ORG) {
            throw new ResourceNotFoundException("This account is not an organization");
        }

        if (organization.getStatus() != OrganizationStatus.ACCEPTED) {
            throw new OrganizationNotAcceptedException("Organization must be accepted to create employees");
        }

        String organizationDomain = extractDomain(organization.getEmail());

        if (!request.email().toLowerCase().endsWith("@" + organizationDomain.toLowerCase())) {
            throw new InvalidEmployeeEmailException("Employee email must belong to the organization's domain: @" + organizationDomain);
        }

        if (employeeRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Employee email already exists");
        }

        if (employeeRepository.existsByOrganizationIdAndUsername(organizationId, request.username())) {
            throw new ResourceAlreadyExistsException("Username already exists for this organization");
        }

        String ldapUid = ldapUserService.createUser(
                organizationId,
                request.username(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.role()
        );


        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .role(request.role())
                .ldapUid(ldapUid)
                .organization(organization)
                .build();


        Employee savedEmployee = employeeRepository.save(employee);

        return toResponse(savedEmployee);
    }

    public EmployeeResponse getProfile(UUID employeeId) {
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return toResponse(employee);
    }

    public List<EmployeeResponse> listByOrganization(UUID organizationId) {
        return employeeRepository
                .findByOrganizationIdOrderByFirstNameAsc(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .organizationId(employee.getOrganization().getId())
                .build();
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new IllegalArgumentException("Organization email has an invalid format");
        }
        return email.substring(atIndex + 1);
    }

    public EmployeeResponse login(EmployeeLoginRequest request) {

        Organization organization = organizationRepository
                .findById(request.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (organization.getRole() != OrganizationRole.ORG) {
            throw new ResourceNotFoundException("This account is not an organization");
        }

        if (organization.getStatus() != OrganizationStatus.ACCEPTED) {
            throw new OrganizationNotAcceptedException("Organization must be accepted to login");
        }

        Employee employee = employeeRepository
                .findByUsernameAndOrganizationId(request.username(), organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        boolean authenticated = ldapUserService.authenticate(employee.getUsername(), request.password(), organization.getId());

        if (!authenticated) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return toResponse(employee);
    }
}