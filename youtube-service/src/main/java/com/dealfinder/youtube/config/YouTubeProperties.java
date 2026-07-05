package com.dealfinder.youtube.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "youtube")
public class YouTubeProperties {
    private Api api = new Api();
    private Search search = new Search();

    @Data
    public static class Api {
        private String key;
        private String baseUrl;
    }

    @Data
    public static class Search {
        private int maxResultsPerQuery = 5;
        private int maxAgeMonths = 12;
        private int minDurationSeconds = 300;
        private int maxDurationSeconds = 1800;
        private List<TrustedChannel> trustedChannels = List.of();
    }

    @Data
    public static class TrustedChannel {
        private String id;
        private String name;
    }
}
