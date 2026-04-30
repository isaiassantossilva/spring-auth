package com.santos.spring_auth.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.santos.spring_auth.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-please-change-me-32bytes-minimum-required";
    private static final String ISSUER = "spring-auth-test";

    private JwtService jwtService;
    private NimbusJwtDecoder decoder;

    @BeforeEach
    void setUp() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        JwtProperties properties = new JwtProperties(SECRET, 60, ISSUER);
        this.jwtService = new JwtService(encoder, properties);
    }

    @Test
    void issueEmitsTokenWithSubjectIssuerAndRoles() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "alice",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = this.jwtService.issue(authentication);

        Jwt jwt = this.decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo("alice");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void issueSetsExpirationFromTtl() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "bob",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = this.jwtService.issue(authentication);

        Jwt jwt = this.decoder.decode(token);
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();
        long ttlSeconds = jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond();
        assertThat(ttlSeconds).isEqualTo(60L * 60L);
    }

    @Test
    void issueWithNoAuthoritiesEmitsEmptyRolesClaim() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("carol", null, List.of());

        String token = this.jwtService.issue(authentication);

        Jwt jwt = this.decoder.decode(token);
        assertThat(jwt.getClaimAsStringList("roles")).isEmpty();
    }
}
