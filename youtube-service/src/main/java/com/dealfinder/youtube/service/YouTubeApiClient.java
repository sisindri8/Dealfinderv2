package com.dealfinder.youtube.service;

import com.dealfinder.youtube.config.YouTubeProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeApiClient {

    private final RestClient youtubeRestClient;
    private final YouTubeProperties props;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(List<SearchItem> items) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SearchItem(SearchId id, SearchSnippet snippet) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SearchId(String videoId) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SearchSnippet(String title, String channelTitle, String publishedAt, Thumbnails thumbnails) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Thumbnails(Thumbnail high, Thumbnail medium) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Thumbnail(String url) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideosResponse(List<VideoItem> items) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record VideoItem(String id, VideoSnippet snippet, ContentDetails contentDetails) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record VideoSnippet(String description) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ContentDetails(String duration) {}
    }

    // Search all channels — no channelId filter
    public SearchResponse searchAllChannels(String query) {
        try {
            return youtubeRestClient.get()
                    .uri(u -> u.path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", query)
                            .queryParam("type", "video")
                            .queryParam("order", "relevance")
                            .queryParam("maxResults", props.getSearch().getMaxResultsPerQuery())
                            .queryParam("key", props.getApi().getKey())
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);
        } catch (Exception e) {
            log.error("YouTube search failed for query='{}': {}", query, e.getMessage());
            return new SearchResponse(List.of());
        }
    }

    public VideosResponse fetchVideoDetails(List<String> videoIds) {
        if (videoIds.isEmpty()) return new VideosResponse(List.of());
        try {
            return youtubeRestClient.get()
                    .uri(u -> u.path("/videos")
                            .queryParam("part", "snippet,contentDetails")
                            .queryParam("id", String.join(",", videoIds))
                            .queryParam("key", props.getApi().getKey())
                            .build())
                    .retrieve()
                    .body(VideosResponse.class);
        } catch (Exception e) {
            log.error("YouTube videos.list failed: {}", e.getMessage());
            return new VideosResponse(List.of());
        }
    }
}
