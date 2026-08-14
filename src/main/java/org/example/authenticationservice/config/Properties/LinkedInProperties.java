package org.example.authenticationservice.config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "linkedin")
public class LinkedInProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}