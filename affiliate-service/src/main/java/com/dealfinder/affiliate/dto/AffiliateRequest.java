package com.dealfinder.affiliate.dto;

import java.util.List;

public record AffiliateRequest(List<ProductLink> productLinks) {
    public record ProductLink(String productName, String amazonUrl) {}
}
