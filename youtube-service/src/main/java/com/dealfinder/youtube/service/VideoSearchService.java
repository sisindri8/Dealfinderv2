package com.dealfinder.youtube.service;

import com.dealfinder.youtube.config.YouTubeProperties;
import com.dealfinder.youtube.dto.VideoSearchRequest;
import com.dealfinder.youtube.dto.VideoSearchResponse;
import com.dealfinder.youtube.dto.VideoSearchResponse.VideoResult;
import com.dealfinder.youtube.dto.VideoSearchResponse.ProductLink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoSearchService {

    private final YouTubeApiClient apiClient;
    private final YouTubeProperties props;
    private final DescriptionParser descriptionParser;

    private static final Set<String> STOP_WORDS = Set.of(
            "best", "top", "under", "below", "above", "review", "reviews",
            "2024", "2025", "2026", "price", "india", "budget", "worth",
            "buying", "buy", "guide", "vs", "comparison", "rupees", "rs",
            "the", "for", "and", "with", "how", "what", "why", "this",
            "that", "from", "are", "you", "your", "my", "sale"
    );

    public VideoSearchResponse search(VideoSearchRequest request) {
        Map<String, YouTubeApiClient.SearchResponse.SearchItem> uniqueVideos = new LinkedHashMap<>();

        for (String query : request.expandedQueries()) {
            var response = apiClient.searchAllChannels(query);
            if (response == null || response.items() == null) continue;
            for (var item : response.items()) {
                if (item.id() != null && item.id().videoId() != null) {
                    uniqueVideos.putIfAbsent(item.id().videoId(), item);
                }
            }
        }

        if (uniqueVideos.isEmpty()) {
            log.info("No videos found for query: {}", request.originalQuery());
            return new VideoSearchResponse(0, List.of());
        }

        log.info("Found {} unique videos before filtering", uniqueVideos.size());

        Set<String> queryKeywords = extractKeywords(request.originalQuery());
        log.info("Query keywords: {}", queryKeywords);

        // Fetch full descriptions + duration
        var videoIds = new ArrayList<>(uniqueVideos.keySet());
        var detailsResponse = apiClient.fetchVideoDetails(videoIds);

        Map<String, YouTubeApiClient.VideosResponse.VideoItem> detailsById = new HashMap<>();
        if (detailsResponse != null && detailsResponse.items() != null) {
            for (var item : detailsResponse.items()) {
                detailsById.put(item.id(), item);
            }
        }

        List<ScoredVideo> scoredVideos = new ArrayList<>();
        int minDur = props.getSearch().getMinDurationSeconds();
        int maxDur = props.getSearch().getMaxDurationSeconds();

        for (Map.Entry<String, YouTubeApiClient.SearchResponse.SearchItem> entry : uniqueVideos.entrySet()) {
            String videoId = entry.getKey();
            var searchItem = entry.getValue();
            var details = detailsById.get(videoId);

            // Year filter: only 2025 and 2026
            String publishedAt = searchItem.snippet() != null ? searchItem.snippet().publishedAt() : null;
            if (!isRecentYear(publishedAt)) {
                log.debug("Skipping old video published: {}", publishedAt);
                continue;
            }

            // Duration filter
            if (details != null && details.contentDetails() != null) {
                int durationSec = parseDuration(details.contentDetails().duration());
                if (durationSec < minDur || durationSec > maxDur) continue;
            }

            String title = searchItem.snippet() != null ? searchItem.snippet().title() : "";
            String description = details != null && details.snippet() != null
                    ? details.snippet().description() : "";

            // Relevance score based on title keywords
            int score = relevanceScore(title, queryKeywords);
            if (score == 0) {
                log.debug("Skipping irrelevant video: '{}'", title);
                continue;
            }

            List<ProductLink> productLinks = descriptionParser.parse(description, title, request.originalQuery());
            if (productLinks.isEmpty()) continue;

            String thumbnailUrl = searchItem.snippet() != null
                    && searchItem.snippet().thumbnails() != null
                    && searchItem.snippet().thumbnails().high() != null
                    ? searchItem.snippet().thumbnails().high().url() : null;

            String channelName = searchItem.snippet() != null
                    ? searchItem.snippet().channelTitle() : "";

            scoredVideos.add(new ScoredVideo(score, new VideoResult(
                    videoId, title, channelName, thumbnailUrl,
                    "https://www.youtube.com/watch?v=" + videoId,
                    productLinks
            )));

            log.info("Relevant video (score={}, year={}): '{}'  links={}", score, publishedAt, title, productLinks.size());
        }

        List<VideoResult> results = scoredVideos.stream()
                .sorted(Comparator.comparingInt(ScoredVideo::score).reversed())
                .map(ScoredVideo::video)
                .collect(Collectors.toList());

        log.info("{} relevant recent videos with product links", results.size());
        return new VideoSearchResponse(results.size(), results);
    }

    private boolean isRecentYear(String publishedAt) {
        if (publishedAt == null) return false;
        return publishedAt.startsWith("2025") || publishedAt.startsWith("2026");
    }

    private Set<String> extractKeywords(String query) {
        Set<String> keywords = new HashSet<>();
        for (String word : query.toLowerCase().split("\\s+")) {
            word = word.replaceAll("[^a-z0-9]", "");
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                keywords.add(word);
            }
        }
        return keywords;
    }

    private int relevanceScore(String title, Set<String> keywords) {
        if (keywords.isEmpty()) return 1;
        String titleLower = title.toLowerCase();
        int score = 0;
        for (String keyword : keywords) {
            if (titleLower.contains(keyword)) score++;
        }
        return score;
    }

    private record ScoredVideo(int score, VideoResult video) {}

    private int parseDuration(String iso8601) {
        if (iso8601 == null) return 0;
        try {
            int total = 0;
            var m = java.util.regex.Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").matcher(iso8601);
            if (m.matches()) {
                if (m.group(1) != null) total += Integer.parseInt(m.group(1)) * 3600;
                if (m.group(2) != null) total += Integer.parseInt(m.group(2)) * 60;
                if (m.group(3) != null) total += Integer.parseInt(m.group(3));
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }
}
