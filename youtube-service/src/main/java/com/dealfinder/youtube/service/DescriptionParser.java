package com.dealfinder.youtube.service;

import com.dealfinder.youtube.dto.VideoSearchResponse.ProductLink;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

@Component
public class DescriptionParser {

    private static final Pattern PRODUCT_LINK_PATTERN = Pattern.compile(
            "([^\\n:]{3,60}?)\\s*[-:]?\\s*(https?://(?:amzn\\.to|www\\.amazon\\.in|amazon\\.in)/\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern AMAZON_URL_PATTERN = Pattern.compile(
            "(https?://(?:amzn\\.to|www\\.amazon\\.in|amazon\\.in)/\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    // Generic single-word equipment labels YouTubers use for their gear links
    private static final Set<String> GENERIC_LABELS = Set.of(
            "camera", "lens", "mic", "microphone", "tripod", "laptop", "monitor",
            "keyboard", "mouse", "light", "lighting", "stand", "gimbal", "drone",
            "bag", "case", "charger", "cable", "adapter", "hub", "ssd", "hdd",
            "phone", "tablet", "headphone", "headphones", "speaker", "printer",
            "router", "modem", "tv", "television", "watch", "smartwatch",
            "headset", "earphone", "earphones", "earbud", "earbuds"
    );

    public List<ProductLink> parse(String description, String videoTitle, String originalQuery) {
        if (description == null || description.isBlank()) return List.of();

        // Extract keywords from video title to filter relevant products
        Set<String> titleKeywords = extractMeaningfulWords(videoTitle);
        Set<String> queryKeywords = extractMeaningfulWords(originalQuery);
        Set<String> allKeywords = new HashSet<>();
        allKeywords.addAll(titleKeywords);
        allKeywords.addAll(queryKeywords);

        List<ProductLink> results = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();

        Matcher matcher = PRODUCT_LINK_PATTERN.matcher(description);
        while (matcher.find()) {
            String productName = matcher.group(1).trim();
            String url = matcher.group(2).trim();
            url = url.replaceAll("[.,;)\\]]+$", "");

            if (seenUrls.contains(url)) continue;
            seenUrls.add(url);

            productName = cleanProductName(productName);
            if (productName.length() < 3) continue;

            // Skip pure generic single-word labels like "Camera:", "Mic:", "Lens:"
            if (isGenericEquipmentLabel(productName)) {
                continue;
            }

            // Check if product is relevant to the video title/query
            if (!isProductRelevant(productName, allKeywords)) {
                continue;
            }

            results.add(new ProductLink(productName, url));
        }

        // Fallback: grab any amazon links if none found
        if (results.isEmpty()) {
            Matcher urlMatcher = AMAZON_URL_PATTERN.matcher(description);
            while (urlMatcher.find()) {
                String url = urlMatcher.group(1).replaceAll("[.,;)\\]]+$", "");
                if (!seenUrls.contains(url)) {
                    seenUrls.add(url);
                    results.add(new ProductLink(videoTitle, url));
                }
            }
        }

        return results.stream().limit(5).toList();
    }

    private boolean isGenericEquipmentLabel(String name) {
        // Single-word generic labels e.g. "LAPTOP", "CAMERA", "MIC"
        String lower = name.toLowerCase().trim();
        if (!lower.contains(" ") && GENERIC_LABELS.contains(lower)) return true;

        // Two-word generic combos e.g. "My Camera", "My Mic", "Camera Used"
        String[] words = lower.split("\\s+");
        if (words.length <= 2) {
            for (String word : words) {
                if (GENERIC_LABELS.contains(word) &&
                        (lower.contains("my ") || lower.contains(" used") || lower.contains(" i use"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isProductRelevant(String productName, Set<String> keywords) {
        if (keywords.isEmpty()) return true;
        String nameLower = productName.toLowerCase();

        // Check direct keyword overlap
        for (String keyword : keywords) {
            if (keyword.length() >= 2 && nameLower.contains(keyword)) return true;
        }

        // Accept if looks like a real product: has both letters + digits (e.g. "Buds 3r", "T310")
        // AND has a capitalized brand-like word (first letter uppercase)
        boolean hasDigits = productName.matches(".*\\d.*");
        boolean hasUppercase = productName.matches(".*[A-Z].*");
        boolean multiWord = productName.contains(" ");

        // Real products usually have model numbers (digits) + brand (uppercase) + are multi-word
        return hasDigits && hasUppercase && multiWord;
    }

    private Set<String> extractMeaningfulWords(String text) {
        Set<String> words = new HashSet<>();
        Set<String> stopWords = Set.of("best", "top", "under", "below", "above", "review",
                "reviews", "2024", "2025", "2026", "price", "india", "budget", "worth",
                "buying", "buy", "guide", "the", "for", "and", "with", "vs", "my", "in",
                "a", "an", "is", "at", "to", "of", "on", "by", "rupees", "rs", "sale");
        for (String word : text.toLowerCase().split("[\\s\\W]+")) {
            if (word.length() >= 2 && !stopWords.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }

    private String cleanProductName(String name) {
        return name.replaceAll("^[\\s\\-:•*#📱💻🎧]+", "")
                .replaceAll("[\\s\\-:•*#]+$", "")
                .trim();
    }
}
