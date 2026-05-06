package com.santos.spring_auth.controller;

import com.santos.spring_auth.dto.auth.LoginResponse;
import com.santos.spring_auth.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements(@SecurityRequirement(name = "Basic Auth"))
    public LoginResponse login(@AuthenticationPrincipal UserDetails user) {
        return this.authService.login(user);
    }
}
