package org.example.authenticationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkedInUserInfoResponse(
        String sub,
        String name,
        String email,
        @JsonProperty("given_name")
        String givenName,
        @JsonProperty("family_name")
        String familyName,
        @JsonProperty("picture")
        String picture
) {}