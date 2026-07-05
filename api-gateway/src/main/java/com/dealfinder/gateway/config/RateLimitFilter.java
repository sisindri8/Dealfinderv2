package com.dealfinder.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Fixed-window rate limiter backed by Redis INCR/EXPIRE. Keys by
 * authenticated userId when present (set by AuthFilter, which runs first),
 * otherwise falls back to client IP. Applies to /api/** only.
 */
@Order(2)
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int requestsPerWindow;
    private final int windowSeconds;

    public RateLimitFilter(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${rate-limit.requests-per-window:20}") int requestsPerWindow,
            @Value("${rate-limit.window-seconds:60}") int windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.requestsPerWindow = requestsPerWindow;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String identity = resolveIdentity(request);
        long windowBucket = Instant.now().getEpochSecond() / windowSeconds;
        String key = "ratelimit:" + identity + ":" + windowBucket;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (count != null && count > requestsPerWindow) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests. Please slow down and try again shortly.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveIdentity(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthFilter.REQUEST_ATTR_USER_ID);
        if (userId != null) {
            return "user:" + userId;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = (forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();
        return "ip:" + ip;
    }
}
