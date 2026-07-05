package com.dealfinder.affiliate.controller;

import com.dealfinder.affiliate.dto.AffiliateRequest;
import com.dealfinder.affiliate.dto.AffiliateResponse;
import com.dealfinder.affiliate.service.AffiliateRewriterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/affiliate")
@RequiredArgsConstructor
public class AffiliateController {

    private final AffiliateRewriterService rewriterService;

    @PostMapping("/rewrite")
    public AffiliateResponse rewrite(@RequestBody AffiliateRequest request) {
        return rewriterService.rewrite(request);
    }
}
