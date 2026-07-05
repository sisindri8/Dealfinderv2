package com.dealfinder.youtube.controller;

import com.dealfinder.youtube.dto.VideoSearchRequest;
import com.dealfinder.youtube.dto.VideoSearchResponse;
import com.dealfinder.youtube.service.VideoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class YouTubeController {

    private final VideoSearchService videoSearchService;

    @PostMapping("/search")
    public VideoSearchResponse search(@RequestBody VideoSearchRequest request) {
        return videoSearchService.search(request);
    }
}
