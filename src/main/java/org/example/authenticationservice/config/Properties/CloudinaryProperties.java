package org.example.authenticationservice.config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudinary")
@Setter
@Getter
public class CloudinaryProperties{
    private String cloudName;
    private String apiKey;
    private String apiSecret;
}