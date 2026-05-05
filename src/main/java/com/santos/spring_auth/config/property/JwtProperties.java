package com.santos.spring_auth.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        long ttlSeconds,
        String issuer
) {}
