package org.example.authenticationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.request.CreateOrganizationRequest;
import org.example.authenticationservice.dto.response.ApiResponse;
import org.example.authenticationservice.dto.response.OrganizationResponse;
import org.example.authenticationservice.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrganizationResponse> create(@Valid @ModelAttribute CreateOrganizationRequest request, @RequestParam("taxRegistrationDocument") MultipartFile taxRegistrationDocument) {
        OrganizationResponse response = organizationService.create(request, taxRegistrationDocument);
        return ApiResponse.success(HttpStatus.CREATED.value(), "Organization created successfully", response);
    }
}