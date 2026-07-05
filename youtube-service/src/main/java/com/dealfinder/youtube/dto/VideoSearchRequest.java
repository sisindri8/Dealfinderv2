package com.dealfinder.youtube.dto;

import java.util.List;

public record VideoSearchRequest(
        String originalQuery,
        List<String> expandedQueries,
        String category
) {}
