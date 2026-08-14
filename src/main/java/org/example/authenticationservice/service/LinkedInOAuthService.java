package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.Properties.LinkedInProperties;
import org.example.authenticationservice.dto.response.LinkedInTokenResponse;
import org.example.authenticationservice.dto.response.LinkedInUserInfoResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class LinkedInOAuthService {

    private final LinkedInProperties linkedInProperties;
    private final RestClient restClient;

    public String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://www.linkedin.com/oauth/v2/authorization")
                .queryParam("response_type", "code")
                .queryParam("client_id", linkedInProperties.getClientId())
                .queryParam("redirect_uri", linkedInProperties.getRedirectUri())
                .queryParam("state", state)
                .queryParam("scope", "openid profile email")
                .build()
                .toUriString();
    }

    public LinkedInTokenResponse exchangeCodeForToken(String code) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", linkedInProperties.getClientId());
        body.add("client_secret", linkedInProperties.getClientSecret());
        body.add("redirect_uri", linkedInProperties.getRedirectUri());

        return restClient
                .post()
                .uri("https://www.linkedin.com/oauth/v2/accessToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(LinkedInTokenResponse.class);
    }

    public LinkedInUserInfoResponse getUserInfo(String accessToken) {
        return restClient
                .get()
                .uri("https://api.linkedin.com/v2/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(LinkedInUserInfoResponse.class);
    }
}