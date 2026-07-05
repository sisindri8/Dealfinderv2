package com.dealfinder.gateway.service;

import com.dealfinder.gateway.dto.SearchResponse;
import com.dealfinder.gateway.dto.SearchResponse.ProductCard;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PipelineService {

    private static final String CACHE_KEY_PREFIX = "search:cache:";

    private final RestClient queryExpansionClient;
    private final RestClient youtubeClient;
    private final RestClient affiliateClient;
    private final RestClient imageClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${affiliate.amazon.tag:your-tag-21}")
    private String amazonTag;

    @Value("${cache.search.ttl-seconds:3600}")
    private long cacheTtlSeconds;

    public PipelineService(
            @Qualifier("queryExpansionClient") RestClient queryExpansionClient,
            @Qualifier("youtubeClient") RestClient youtubeClient,
            @Qualifier("affiliateClient") RestClient affiliateClient,
            @Qualifier("imageClient") RestClient imageClient,
            RedisTemplate<String, Object> redisTemplate) {
        this.queryExpansionClient = queryExpansionClient;
        this.youtubeClient = youtubeClient;
        this.affiliateClient = affiliateClient;
        this.imageClient = imageClient;
        this.redisTemplate = redisTemplate;
    }

    // ── Downstream DTOs ─────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExpansionResponse(String originalQuery, List<String> expandedQueries, String category) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoSearchRequest(String originalQuery, List<String> expandedQueries, String category) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoSearchResponse(int totalVideos, List<VideoResult> videos) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record VideoResult(String videoId, String title, String channelName,
                           String thumbnailUrl, String videoUrl, List<ProductLink> productLinks) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        record ProductLink(String productName, String amazonUrl) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AffiliateRequest(List<AffiliateProductLink> productLinks) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record AffiliateProductLink(String productName, String amazonUrl) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AffiliateResponse(List<AffiliateProduct> products) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record AffiliateProduct(String productName, String affiliateUrl, String asin) {}
    }

    // ── Internal ranking model ───────────────────────────────────────────

    private record ProductMention(
            String productName,
            String affiliateUrl,
            String asin,
            String videoTitle,
            String videoUrl,
            String channelName,
            String videoThumbnailUrl
    ) {}

    // ── Pipeline ─────────────────────────────────────────────────────────

    public SearchResponse search(String query) {
        String cacheKey = CACHE_KEY_PREFIX + normalizeForCacheKey(query);

        SearchResponse cached = getCached(cacheKey);
        if (cached != null) {
            log.info("Cache hit for query: {}", query);
            return cached;
        }

        SearchResponse response = runPipeline(query);

        // Only cache genuinely successful results, not empty fallbacks from
        // a downstream outage — an empty cached entry would keep serving
        // "no products" for an hour even after the outage clears.
        if (!response.products().isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofSeconds(cacheTtlSeconds));
        }

        return response;
    }

    private SearchResponse getCached(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof SearchResponse sr) {
                return sr;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed, falling back to live search: {}", e.getMessage());
        }
        return null;
    }

    private String normalizeForCacheKey(String query) {
        return query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private SearchResponse runPipeline(String query) {
        log.info("Pipeline start for query: {}", query);

        // Step 1: Query expansion
        ExpansionResponse expansion = expandQuery(query);
        log.info("Expanded to {} queries", expansion.expandedQueries().size());

        // Step 2: YouTube search
        VideoSearchResponse videos = searchVideos(expansion);
        log.info("YouTube returned {} videos with product links", videos.totalVideos());

        if (videos.videos() == null || videos.videos().isEmpty()) {
            return new SearchResponse(query, 0, List.of());
        }

        // Step 3: Collect all products from ALL videos, rewrite affiliate links
        // Key: ASIN → list of mentions (tracks which videos mentioned this product)
        Map<String, List<ProductMention>> productsByAsin = new LinkedHashMap<>();

        for (VideoSearchResponse.VideoResult video : videos.videos()) {
            if (video.productLinks() == null || video.productLinks().isEmpty()) continue;

            var affiliateLinks = video.productLinks().stream()
                    .map(pl -> new AffiliateRequest.AffiliateProductLink(pl.productName(), pl.amazonUrl()))
                    .toList();

            AffiliateResponse affiliateResponse = rewriteLinks(affiliateLinks);
            if (affiliateResponse == null || affiliateResponse.products() == null) continue;

            for (AffiliateResponse.AffiliateProduct product : affiliateResponse.products()) {
                String asin = product.asin();
                ProductMention mention = new ProductMention(
                        product.productName(),
                        product.affiliateUrl(),
                        asin,
                        video.title(),
                        video.videoUrl(),
                        video.channelName(),
                        video.thumbnailUrl()
                );
                productsByAsin.computeIfAbsent(asin, k -> new ArrayList<>()).add(mention);
            }
        }

        log.info("Collected {} unique products across all videos", productsByAsin.size());

        // Step 4: Rank products by mention count (most mentioned = most trusted)
        List<Map.Entry<String, List<ProductMention>>> ranked = productsByAsin.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .limit(5)
                .collect(Collectors.toList());

        // Step 5: Assign category labels
        String[][] categories = {
                {"Best Overall",    "🏆"},
                {"Editor's Choice", "⭐"},
                {"Best Value",      "💰"},
                {"Most Popular",    "🔥"},
                {"Budget Pick",     "👍"}
        };

        List<ProductCard> cards = new ArrayList<>();

        for (int i = 0; i < ranked.size(); i++) {
            var entry = ranked.get(i);
            String asin = entry.getKey();
            List<ProductMention> mentions = entry.getValue();
            ProductMention best = mentions.get(0); // primary mention (first video that cited it)

            String categoryName = categories[i][0];
            String categoryEmoji = categories[i][1];
            int mentionCount = mentions.size();

            // Fetch image — scrapes Amazon product page directly using ASIN
            String imageUrl = fetchImage(best.productName(), asin);

            log.info("[{}] {} '{}' - mentioned in {} video(s)",
                    categoryEmoji, categoryName, best.productName(), mentionCount);

            cards.add(new ProductCard(
                    best.productName(),
                    best.affiliateUrl(),
                    asin,
                    imageUrl,
                    categoryName,
                    categoryEmoji,
                    mentionCount,
                    best.videoTitle(),
                    best.videoUrl(),
                    best.channelName(),
                    best.videoThumbnailUrl()
            ));
        }

        log.info("Pipeline complete: {} ranked products", cards.size());
        return new SearchResponse(query, cards.size(), cards);
    }

    private String buildAmazonImageUrl(String asin) {
        return "https://ws-in.amazon-adsystem.com/widgets/q?_encoding=UTF8&ASIN="
                + asin + "&Format=_SL250_&ID=AsinImage&MarketPlace=IN"
                + "&ServiceVersion=20070822&WS=1&tag=" + amazonTag;
    }

    private ExpansionResponse expandQuery(String query) {
        try {
            return queryExpansionClient.post()
                    .uri("/api/v1/expand")
                    .body(Map.of("query", query))
                    .retrieve()
                    .body(ExpansionResponse.class);
        } catch (Exception e) {
            log.error("Query expansion failed: {}", e.getMessage());
            return new ExpansionResponse(query, List.of(query), "general");
        }
    }

    private VideoSearchResponse searchVideos(ExpansionResponse expansion) {
        try {
            return youtubeClient.post()
                    .uri("/api/v1/videos/search")
                    .body(new VideoSearchRequest(
                            expansion.originalQuery(),
                            expansion.expandedQueries(),
                            expansion.category()))
                    .retrieve()
                    .body(VideoSearchResponse.class);
        } catch (Exception e) {
            log.error("YouTube search failed: {}", e.getMessage());
            return new VideoSearchResponse(0, List.of());
        }
    }

    private AffiliateResponse rewriteLinks(List<AffiliateRequest.AffiliateProductLink> links) {
        try {
            return affiliateClient.post()
                    .uri("/api/v1/affiliate/rewrite")
                    .body(new AffiliateRequest(links))
                    .retrieve()
                    .body(AffiliateResponse.class);
        } catch (Exception e) {
            log.error("Affiliate rewrite failed: {}", e.getMessage());
            return new AffiliateResponse(List.of());
        }
    }

    private String fetchImage(String productName, String asin) {
        try {
            String finalAsin = asin;
            var result = imageClient.get()
                    .uri(u -> u.path("/api/v1/image/search")
                            .queryParam("productName", productName)
                            .queryParam("asin", finalAsin != null ? finalAsin : "")
                            .build())
                    .retrieve()
                    .body(Map.class);
            if (result != null) {
                String url = (String) result.get("imageUrl");
                return (url != null && !url.isBlank()) ? url : null;
            }
        } catch (Exception e) {
            log.warn("Image fetch failed for '{}': {}", productName, e.getMessage());
        }
        return null;
    }
}
