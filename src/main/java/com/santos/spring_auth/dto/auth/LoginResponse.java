package com.santos.spring_auth.dto.auth;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds
) {}
