package com.santos.spring_auth.dto.auth;

public record LoginResponseDTO(
        String tokenType,
        String accessToken,
        long expiresInSeconds
) {}
