package com.dealfinder.gateway.dto;

public record FavoriteProduct(
        String asin,
        String productName,
        String affiliateUrl,
        String imageUrl,
        long savedAt
) {}
