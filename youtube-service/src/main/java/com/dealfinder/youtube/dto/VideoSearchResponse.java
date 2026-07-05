package com.dealfinder.youtube.dto;

import java.util.List;

public record VideoSearchResponse(
        int totalVideos,
        List<VideoResult> videos
) {
    public record VideoResult(
            String videoId,
            String title,
            String channelName,
            String thumbnailUrl,
            String videoUrl,
            List<ProductLink> productLinks
    ) {}

    public record ProductLink(
            String productName,
            String amazonUrl
    ) {}
}
