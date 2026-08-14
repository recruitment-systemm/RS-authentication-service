package org.example.authenticationservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkedInTokenResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") Long expiresIn, String scope) { }