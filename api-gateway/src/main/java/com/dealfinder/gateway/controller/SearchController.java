package com.dealfinder.gateway.controller;

import com.dealfinder.gateway.config.AuthFilter;
import com.dealfinder.gateway.dto.SearchRequest;
import com.dealfinder.gateway.dto.SearchResponse;
import com.dealfinder.gateway.service.PipelineService;
import com.dealfinder.gateway.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final PipelineService pipelineService;
    private final UserService userService;

    @PostMapping
    public SearchResponse search(HttpServletRequest httpRequest, @RequestBody SearchRequest request) {
        String userId = (String) httpRequest.getAttribute(AuthFilter.REQUEST_ATTR_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required to search");
        }

        SearchResponse response = pipelineService.search(request.query());
        userService.recordSearch(userId, request.query());
        return response;
    }
}
