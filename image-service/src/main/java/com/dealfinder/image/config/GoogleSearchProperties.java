package com.dealfinder.image.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "google.custom-search")
public class GoogleSearchProperties {
    private String apiKey;
    private String searchEngineId;
    private String baseUrl = "https://www.googleapis.com/customsearch/v1";
}
