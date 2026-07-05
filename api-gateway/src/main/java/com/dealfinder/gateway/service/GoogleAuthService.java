package com.dealfinder.gateway.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Service
public class GoogleAuthService {

    public record GoogleUserInfo(String sub, String email, String name, String pictureUrl) {}

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(@Value("${google.oauth.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    /**
     * Verifies the ID token's signature, issuer, audience, and expiry.
     * Returns the extracted profile if valid, or empty if verification fails.
     */
    public GoogleUserInfo verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Google ID token failed verification");
                return null;
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String sub = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            return new GoogleUserInfo(sub, email, name, picture);
        } catch (GeneralSecurityException | java.io.IOException | IllegalArgumentException e) {
            log.warn("Error verifying Google ID token: {}", e.getMessage());
            return null;
        }
    }
}
