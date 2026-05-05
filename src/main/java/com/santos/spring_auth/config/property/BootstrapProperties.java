package com.santos.spring_auth.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(
        String adminUsername,
        String adminPassword,
        String adminEmail
) {}
