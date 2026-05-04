package com.denys.externalapi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
    @GetMapping("/api/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "message", "Це секретні дані з External API!",
                "user_id", jwt.getSubject());
    }
}
