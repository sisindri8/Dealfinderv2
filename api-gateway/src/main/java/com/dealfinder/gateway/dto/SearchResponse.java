package com.dealfinder.gateway.dto;

import java.util.List;

public record SearchResponse(
        String query,
        int totalProducts,
        List<ProductCard> products
) {
    public record ProductCard(
            String productName,
            String affiliateUrl,
            String asin,
            String imageUrl,
            String category,         // e.g. "Best Overall", "Best Value", etc.
            String categoryEmoji,    // e.g. "🏆", "💰", etc.
            int mentionCount,        // how many videos mentioned this product
            String videoTitle,
            String videoUrl,
            String channelName,
            String videoThumbnailUrl
    ) {}
}
