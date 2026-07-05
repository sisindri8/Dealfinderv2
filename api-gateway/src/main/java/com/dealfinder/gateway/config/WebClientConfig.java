package com.dealfinder.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClientConfig {

    @Bean("queryExpansionClient")
    public RestClient queryExpansionClient(@Value("${services.query-expansion.base-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean("youtubeClient")
    public RestClient youtubeClient(@Value("${services.youtube.base-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean("affiliateClient")
    public RestClient affiliateClient(@Value("${services.affiliate.base-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean("imageClient")
    public RestClient imageClient(@Value("${services.image.base-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }
}
