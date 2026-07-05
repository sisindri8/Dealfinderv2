package com.dealfinder.queryexpansion.controller;

import com.dealfinder.queryexpansion.dto.QueryExpansionRequest;
import com.dealfinder.queryexpansion.dto.QueryExpansionResponse;
import com.dealfinder.queryexpansion.service.RuleBasedExpansionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expand")
@RequiredArgsConstructor
public class QueryExpansionController {

    private final RuleBasedExpansionService expansionService;

    @PostMapping
    public QueryExpansionResponse expand(@RequestBody QueryExpansionRequest request) {
        return expansionService.expand(request.query());
    }
}
