package com.dealfinder.youtube.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClientConfig {

    @Bean
    public RestClient youtubeRestClient(YouTubeProperties props) {
        return RestClient.builder()
                .baseUrl(props.getApi().getBaseUrl())
                .build();
    }

    @Bean
    public RestClient genericRestClient() {
        return RestClient.builder().build();
    }
}
