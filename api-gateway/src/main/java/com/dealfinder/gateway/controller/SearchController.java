package com.dealfinder.gateway.controller;

import com.dealfinder.gateway.config.AuthFilter;
import com.dealfinder.gateway.dto.SearchRequest;
import com.dealfinder.gateway.dto.SearchResponse;
import com.dealfinder.gateway.service.PipelineService;
import com.dealfinder.gateway.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final PipelineService pipelineService;
    private final UserService userService;

    @PostMapping
    public SearchResponse search(HttpServletRequest httpRequest, @RequestBody SearchRequest request) {
        SearchResponse response = pipelineService.search(request.query());

        String userId = (String) httpRequest.getAttribute(AuthFilter.REQUEST_ATTR_USER_ID);
        if (userId != null) {
            userService.recordSearch(userId, request.query());
        }

        return response;
    }
}
