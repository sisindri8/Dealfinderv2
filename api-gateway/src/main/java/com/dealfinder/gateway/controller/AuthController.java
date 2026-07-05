package com.dealfinder.gateway.controller;

import com.dealfinder.gateway.config.AuthFilter;
import com.dealfinder.gateway.dto.AuthDtos.AuthResponse;
import com.dealfinder.gateway.dto.AuthDtos.ErrorResponse;
import com.dealfinder.gateway.dto.AuthDtos.GoogleLoginRequest;
import com.dealfinder.gateway.dto.AuthDtos.UserProfile;
import com.dealfinder.gateway.service.GoogleAuthService;
import com.dealfinder.gateway.service.GoogleAuthService.GoogleUserInfo;
import com.dealfinder.gateway.service.JwtService;
import com.dealfinder.gateway.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        if (request.idToken() == null || request.idToken().isBlank()) {
            return ResponseEntity.badRequest().body(errorBody("idToken is required"));
        }

        GoogleUserInfo googleUser = googleAuthService.verify(request.idToken());
        if (googleUser == null) {
            return ResponseEntity.status(401).body(errorBody("Invalid Google ID token"));
        }

        UserProfile profile = userService.upsertUser(googleUser);
        String sessionToken = jwtService.generateToken(profile.userId(), profile.email());

        log.info("Google sign-in successful for user {}", profile.userId());
        return ResponseEntity.ok(new AuthResponse(sessionToken, profile));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        String userId = (String) request.getAttribute(AuthFilter.REQUEST_ATTR_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(401).body(errorBody("Not authenticated"));
        }
        UserProfile profile = userService.getUser(userId);
        if (profile == null) {
            return ResponseEntity.status(404).body(errorBody("User not found"));
        }
        return ResponseEntity.ok(profile);
    }

    private ErrorResponse errorBody(String message) {
        return new ErrorResponse(message);
    }
}
