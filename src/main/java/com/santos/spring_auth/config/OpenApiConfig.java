package com.santos.spring_auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "spring-auth API", version = "v1"),
        security = @SecurityRequirement(name = "Bearer Auth")
)
@SecuritySchemes({
        @SecurityScheme(
                name = "Bearer Auth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = "JWT authentication for all endpoints except login"
        ),
        @SecurityScheme(
                name = "Basic Auth",
                type = SecuritySchemeType.HTTP,
                scheme = "basic",
                description = "Basic authentication only for login endpoint"
        )
})
public class OpenApiConfig {
}
