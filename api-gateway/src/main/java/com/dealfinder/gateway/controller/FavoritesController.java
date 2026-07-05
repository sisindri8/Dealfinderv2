package com.dealfinder.gateway.controller;

import com.dealfinder.gateway.config.AuthFilter;
import com.dealfinder.gateway.dto.AuthDtos.ErrorResponse;
import com.dealfinder.gateway.dto.FavoriteProduct;
import com.dealfinder.gateway.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoritesController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        String userId = requireUserId(request);
        if (userId == null) return unauthorized();
        List<FavoriteProduct> favorites = userService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    public ResponseEntity<?> add(HttpServletRequest request, @RequestBody FavoriteProduct product) {
        String userId = requireUserId(request);
        if (userId == null) return unauthorized();
        if (product.asin() == null || product.asin().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("asin is required"));
        }
        FavoriteProduct toSave = new FavoriteProduct(
                product.asin(), product.productName(), product.affiliateUrl(),
                product.imageUrl(), System.currentTimeMillis());
        userService.addFavorite(userId, toSave);
        return ResponseEntity.ok(toSave);
    }

    @DeleteMapping("/{asin}")
    public ResponseEntity<?> remove(HttpServletRequest request, @PathVariable String asin) {
        String userId = requireUserId(request);
        if (userId == null) return unauthorized();
        userService.removeFavorite(userId, asin);
        return ResponseEntity.noContent().build();
    }

    private String requireUserId(HttpServletRequest request) {
        return (String) request.getAttribute(AuthFilter.REQUEST_ATTR_USER_ID);
    }

    private ResponseEntity<ErrorResponse> unauthorized() {
        return ResponseEntity.status(401).body(new ErrorResponse("Sign in required"));
    }
}
