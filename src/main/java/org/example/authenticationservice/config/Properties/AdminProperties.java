package org.example.authenticationservice.config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@Getter
@Setter
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {
    private UUID id;
    private String email;
    private String password;
}