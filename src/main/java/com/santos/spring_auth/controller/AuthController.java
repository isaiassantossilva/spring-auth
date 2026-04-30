package com.santos.spring_auth.controller;

import com.santos.spring_auth.config.JwtProperties;
import com.santos.spring_auth.dto.auth.LoginRequest;
import com.santos.spring_auth.dto.auth.LoginResponse;
import com.santos.spring_auth.dto.user.UserResponse;
import com.santos.spring_auth.gateway.AuthenticatedUserGateway;
import com.santos.spring_auth.mapper.UserMapper;
import com.santos.spring_auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticatedUserGateway authenticatedUserGateway;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String token = this.jwtService.issue(authentication);
        return new LoginResponse("Bearer", token, TimeUnit.MINUTES.toSeconds(this.jwtProperties.ttlMinutes()));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return this.userMapper.toResponse(this.authenticatedUserGateway.current());
    }
}
