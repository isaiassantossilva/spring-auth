package com.santos.spring_auth.service;

import com.santos.spring_auth.config.property.JwtProperties;
import com.santos.spring_auth.dto.auth.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponseDTO login(UserDetails user) {
        String accessToken = this.jwtService.generateToken(user);
        return new LoginResponseDTO("Bearer", accessToken, this.jwtProperties.ttlSeconds());
    }
}
