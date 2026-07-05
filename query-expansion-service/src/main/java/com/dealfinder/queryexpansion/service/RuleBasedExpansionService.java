package com.dealfinder.queryexpansion.service;

import com.dealfinder.queryexpansion.dto.QueryExpansionResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RuleBasedExpansionService {

    private static final Map<String, String> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry("phone", "mobiles"),
            Map.entry("mobile", "mobiles"),
            Map.entry("laptop", "laptops"),
            Map.entry("earbuds", "earphones"),
            Map.entry("earphones", "earphones"),
            Map.entry("headphones", "earphones"),
            Map.entry("tws", "earphones"),
            Map.entry("smartwatch", "wearables"),
            Map.entry("watch", "wearables"),
            Map.entry("tv", "televisions"),
            Map.entry("television", "televisions"),
            Map.entry("camera", "cameras"),
            Map.entry("tablet", "tablets")
    );

    public QueryExpansionResponse expand(String originalQuery) {
        String q = originalQuery.toLowerCase().trim();
        String category = detectCategory(q);
        List<String> queries = new ArrayList<>();

        queries.add(originalQuery);
        queries.add("best " + originalQuery);
        queries.add(originalQuery + " review");
        queries.add("top " + originalQuery);
        queries.add(originalQuery + " 2024");

        if (category != null) {
            queries.add("best " + category + " " + extractBudget(q));
        }

        List<String> unique = queries.stream().distinct().limit(5).toList();
        return new QueryExpansionResponse(originalQuery, unique, category != null ? category : "general");
    }

    private String detectCategory(String q) {
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (q.contains(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    private String extractBudget(String q) {
        if (q.contains("under") || q.contains("below")) {
            int idx = Math.max(q.indexOf("under"), q.indexOf("below"));
            return q.substring(idx).trim();
        }
        return "";
    }
}
