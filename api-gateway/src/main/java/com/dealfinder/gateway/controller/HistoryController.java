package com.dealfinder.gateway.controller;

import com.dealfinder.gateway.config.AuthFilter;
import com.dealfinder.gateway.dto.AuthDtos.ErrorResponse;
import com.dealfinder.gateway.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistoryController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        String userId = (String) request.getAttribute(AuthFilter.REQUEST_ATTR_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Sign in required"));
        }
        List<String> history = userService.getHistory(userId);
        return ResponseEntity.ok(history);
    }
}
