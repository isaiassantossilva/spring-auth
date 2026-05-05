package com.santos.spring_auth.service;

import com.santos.spring_auth.config.property.JwtProperties;
import com.santos.spring_auth.dto.auth.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponse login(UserDetails user) {
        String accessToken = this.jwtService.generateToken(user);
        return new LoginResponse("Bearer", accessToken, this.jwtProperties.ttlSeconds());
    }
}
