package com.dealfinder.gateway.config;

import com.dealfinder.gateway.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads "Authorization: Bearer <token>" if present and, when valid, attaches
 * the resolved userId as a request attribute. Does NOT reject unauthenticated
 * requests itself — search stays public. Controllers for user-scoped
 * endpoints (favorites, history, /auth/me) check REQUEST_ATTR_USER_ID and
 * return 401 themselves if it's missing.
 */
@Order(1)
@Component
public class AuthFilter extends OncePerRequestFilter {

    public static final String REQUEST_ATTR_USER_ID = "userId";

    private final JwtService jwtService;

    public AuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String userId = jwtService.validateAndGetUserId(token);
            if (userId != null) {
                request.setAttribute(REQUEST_ATTR_USER_ID, userId);
            }
        }
        chain.doFilter(request, response);
    }
}
