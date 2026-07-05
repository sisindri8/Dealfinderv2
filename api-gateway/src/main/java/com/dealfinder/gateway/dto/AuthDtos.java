package com.dealfinder.gateway.dto;

public class AuthDtos {

    public record GoogleLoginRequest(String idToken) {}

    public record UserProfile(String userId, String email, String name, String pictureUrl) {}

    public record AuthResponse(String token, UserProfile user) {}

    public record ErrorResponse(String error) {}
}
