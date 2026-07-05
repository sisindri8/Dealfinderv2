package com.dealfinder.image.controller;

import com.dealfinder.image.service.GoogleImageSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final GoogleImageSearchService imageSearchService;

    @GetMapping("/search")
    public Map<String, String> search(
            @RequestParam String productName,
            @RequestParam(required = false, defaultValue = "") String asin) {
        String imageUrl = imageSearchService.findImage(productName, asin);
        return Map.of("productName", productName, "imageUrl", imageUrl != null ? imageUrl : "");
    }
}
