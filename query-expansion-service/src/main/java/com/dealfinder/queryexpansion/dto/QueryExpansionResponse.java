package com.dealfinder.queryexpansion.dto;

import java.util.List;

public record QueryExpansionResponse(
        String originalQuery,
        List<String> expandedQueries,
        String category
) {}
