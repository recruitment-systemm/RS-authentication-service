package org.example.authenticationservice.config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Setter
@Getter
public class JWTProperties {
    private String secret;
    private int accessExpiration;
    private int refreshExpiration;
}
