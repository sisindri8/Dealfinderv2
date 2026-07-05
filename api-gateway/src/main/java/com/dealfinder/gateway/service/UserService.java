package com.dealfinder.gateway.service;

import com.dealfinder.gateway.dto.AuthDtos.UserProfile;
import com.dealfinder.gateway.dto.FavoriteProduct;
import com.dealfinder.gateway.service.GoogleAuthService.GoogleUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores user profiles, favorites, and search history in Redis.
 *
 * Note: Redis is used here for simplicity since it's already part of the
 * stack. It's fine for an MVP, but Redis is not meant as a durable primary
 * datastore (eviction policies / restarts can lose data depending on plan).
 * For production-grade persistence, migrate this to Postgres down the line.
 */
@Slf4j
@Service
public class UserService {

    private static final String USER_KEY_PREFIX = "user:";
    private static final String FAVORITES_KEY_PREFIX = "favorites:";
    private static final String HISTORY_KEY_PREFIX = "history:";
    private static final int MAX_HISTORY_ENTRIES = 50;
    private static final Duration HISTORY_TTL = Duration.ofDays(180);
    private static final Duration FAVORITES_TTL = Duration.ofDays(365);

    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ── User profile ─────────────────────────────────────────────────────

    public UserProfile upsertUser(GoogleUserInfo googleUser) {
        String key = USER_KEY_PREFIX + googleUser.sub();
        Map<Object, Object> fields = new HashMap<>();
        fields.put("email", googleUser.email());
        fields.put("name", googleUser.name());
        fields.put("pictureUrl", googleUser.pictureUrl());
        redisTemplate.opsForHash().putAll(key, fields);
        // Only set createdAt the first time this user signs in
        redisTemplate.opsForHash().putIfAbsent(key, "createdAt", System.currentTimeMillis());
        return new UserProfile(googleUser.sub(), googleUser.email(), googleUser.name(), googleUser.pictureUrl());
    }

    public UserProfile getUser(String userId) {
        String key = USER_KEY_PREFIX + userId;
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(key);
        if (fields.isEmpty()) return null;
        return new UserProfile(
                userId,
                (String) fields.get("email"),
                (String) fields.get("name"),
                (String) fields.get("pictureUrl"));
    }

    // ── Favorites ────────────────────────────────────────────────────────

    public void addFavorite(String userId, FavoriteProduct product) {
        String key = FAVORITES_KEY_PREFIX + userId;
        redisTemplate.opsForHash().put(key, product.asin(), product);
        redisTemplate.expire(key, FAVORITES_TTL);
    }

    public void removeFavorite(String userId, String asin) {
        String key = FAVORITES_KEY_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, asin);
    }

    public List<FavoriteProduct> getFavorites(String userId) {
        String key = FAVORITES_KEY_PREFIX + userId;
        List<Object> values = redisTemplate.opsForHash().values(key);
        return values.stream()
                .map(v -> (FavoriteProduct) v)
                .sorted(Comparator.comparingLong(FavoriteProduct::savedAt).reversed())
                .collect(Collectors.toList());
    }

    // ── Search history ───────────────────────────────────────────────────

    public void recordSearch(String userId, String query) {
        String key = HISTORY_KEY_PREFIX + userId;
        redisTemplate.opsForList().remove(key, 0, query); // dedupe existing entry
        redisTemplate.opsForList().leftPush(key, query);
        redisTemplate.opsForList().trim(key, 0, MAX_HISTORY_ENTRIES - 1);
        redisTemplate.expire(key, HISTORY_TTL);
    }

    public List<String> getHistory(String userId) {
        String key = HISTORY_KEY_PREFIX + userId;
        List<Object> entries = redisTemplate.opsForList().range(key, 0, MAX_HISTORY_ENTRIES - 1);
        if (entries == null) return List.of();
        return entries.stream().map(String::valueOf).collect(Collectors.toList());
    }
}
