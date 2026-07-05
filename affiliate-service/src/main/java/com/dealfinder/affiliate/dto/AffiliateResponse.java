package com.dealfinder.affiliate.dto;

import java.util.List;

public record AffiliateResponse(List<AffiliateProduct> products) {
    public record AffiliateProduct(String productName, String affiliateUrl, String asin) {}
}
