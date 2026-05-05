package com.santos.spring_auth.service;

import com.santos.spring_auth.config.property.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLES = "roles";
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(this.jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(this.jwtProperties.ttlSeconds(), ChronoUnit.SECONDS))
                .subject(user.getUsername())
                .claim(ROLES, roles)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
