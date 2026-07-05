package com.dealfinder.image.service;

import com.dealfinder.image.config.GoogleSearchProperties;
import com.dealfinder.image.dto.GoogleCustomSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves a product image for a given product name / ASIN.
 *
 * Primary source: Google Custom Search JSON API (searchType=image), keyed by product name.
 * This avoids scraping Amazon directly, which Amazon actively blocks/CAPTCHAs in production.
 *
 * Fallback: best-effort scrape of the Amazon product page's og:image tag, only used if
 * Google Custom Search isn't configured or returns nothing.
 *
 * Successful lookups are cached in-memory for the lifetime of the instance to avoid
 * burning through the Google Custom Search free quota (100 queries/day) on repeat queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleImageSearchService {

    private final GoogleSearchProperties googleSearchProperties;
    private final RestClient restClient = RestClient.builder().build();

    // Simple in-memory cache: productName+asin -> imageUrl (or empty string for "known miss")
    private final ConcurrentHashMap<String, String> imageCache = new ConcurrentHashMap<>();

    private static final Pattern OG_IMAGE_PATTERN = Pattern.compile(
            "<meta[^>]+property=[\"']og:image[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    public String findImage(String productName, String asin) {
        String cacheKey = (productName == null ? "" : productName.trim().toLowerCase()) + "|" + (asin == null ? "" : asin);
        String cached = imageCache.get(cacheKey);
        if (cached != null) {
            return cached.isBlank() ? null : cached;
        }

        String imageUrl = searchGoogleCustomSearch(productName);

        if (imageUrl == null && asin != null && !asin.isBlank()) {
            // Fallback: try scraping the Amazon page directly (best-effort, may be blocked)
            imageUrl = scrapeAmazonImage(asin);
        }

        imageCache.put(cacheKey, imageUrl == null ? "" : imageUrl);

        if (imageUrl == null) {
            log.warn("Could not find image for: {} (ASIN: {})", productName, asin);
        }
        return imageUrl;
    }

    private String searchGoogleCustomSearch(String productName) {
        if (googleSearchProperties.getApiKey() == null || googleSearchProperties.getApiKey().isBlank()
                || googleSearchProperties.getSearchEngineId() == null || googleSearchProperties.getSearchEngineId().isBlank()) {
            log.debug("Google Custom Search not configured (missing GOOGLE_API_KEY / GOOGLE_SEARCH_ENGINE_ID); skipping.");
            return null;
        }
        if (productName == null || productName.isBlank()) {
            return null;
        }

        try {
            String encodedQuery = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String uri = googleSearchProperties.getBaseUrl()
                    + "?key=" + googleSearchProperties.getApiKey()
                    + "&cx=" + googleSearchProperties.getSearchEngineId()
                    + "&q=" + encodedQuery
                    + "&searchType=image"
                    + "&imgType=photo"
                    + "&safe=active"
                    + "&num=3";

            GoogleCustomSearchResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(GoogleCustomSearchResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty()) {
                return null;
            }

            List<GoogleCustomSearchResponse.Item> items = response.items();
            String url = items.get(0).link();
            log.info("Google Custom Search found image for '{}'", productName);
            return url;

        } catch (Exception e) {
            log.error("Google Custom Search image lookup failed for '{}': {}", productName, e.getMessage());
            return null;
        }
    }

    private String scrapeAmazonImage(String asin) {
        try {
            String html = restClient.get()
                    .uri("https://www.amazon.in/dp/" + asin)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-IN,en;q=0.9")
                    .retrieve()
                    .body(String.class);

            if (html == null) return null;

            Matcher m = OG_IMAGE_PATTERN.matcher(html);
            if (m.find()) {
                String url = m.group(1);
                if (url.contains("amazon") || url.contains("media-amazon")) {
                    log.info("Got Amazon og:image fallback for ASIN {}", asin);
                    return url;
                }
            }
        } catch (Exception e) {
            log.debug("Amazon fallback scrape failed for ASIN {}: {}", asin, e.getMessage());
        }
        return null;
    }
}
