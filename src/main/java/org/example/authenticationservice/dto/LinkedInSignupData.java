package org.example.authenticationservice.dto;

public record LinkedInSignupData(
        String linkedinId,
        String name,
        String email,
        String givenName,
        String familyName,
        String picture
) { }