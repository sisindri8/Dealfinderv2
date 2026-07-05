package com.dealfinder.affiliate.service;

import com.dealfinder.affiliate.dto.AffiliateRequest;
import com.dealfinder.affiliate.dto.AffiliateResponse;
import com.dealfinder.affiliate.dto.AffiliateResponse.AffiliateProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AffiliateRewriterService {

    private static final Pattern ASIN_PATTERN = Pattern.compile(
            "/(?:dp|gp/product|exec/obidos/ASIN)/([A-Z0-9]{10})"
    );

    @Value("${affiliate.amazon.tag}")
    private String affiliateTag;

    @Value("${affiliate.amazon.base-url}")
    private String amazonBaseUrl;

    private final RestClient restClient = RestClient.builder().build();

    public AffiliateResponse rewrite(AffiliateRequest request) {
        List<AffiliateProduct> products = new ArrayList<>();

        for (AffiliateRequest.ProductLink link : request.productLinks()) {
            try {
                String resolvedUrl = resolveShortUrl(link.amazonUrl());
                String asin = extractAsin(resolvedUrl);

                if (asin == null) {
                    log.warn("Could not extract ASIN from: {}", resolvedUrl);
                    continue;
                }

                String affiliateUrl = amazonBaseUrl + "/" + asin + "?tag=" + affiliateTag;
                products.add(new AffiliateProduct(link.productName(), affiliateUrl, asin));

            } catch (Exception e) {
                log.error("Failed to process link {}: {}", link.amazonUrl(), e.getMessage());
            }
        }

        return new AffiliateResponse(products);
    }

    private String resolveShortUrl(String url) {
        if (url.contains("amazon.in") || url.contains("amazon.com")) return url;
        try {
            var response = restClient.method(HttpMethod.HEAD)
                    .uri(URI.create(url))
                    .retrieve()
                    .toBodilessEntity();
            if (response.getHeaders().getLocation() != null) {
                return response.getHeaders().getLocation().toString();
            }
        } catch (Exception e) {
            log.warn("Could not follow redirect for {}: {}", url, e.getMessage());
        }
        return url;
    }

    private String extractAsin(String url) {
        Matcher m = ASIN_PATTERN.matcher(url);
        return m.find() ? m.group(1) : null;
    }
}
