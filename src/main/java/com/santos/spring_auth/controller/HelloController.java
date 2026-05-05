package com.santos.spring_auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('USER')")
    public String hello(Authentication authentication) {
        return "Hello, %s".formatted(authentication.getName());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin(Authentication authentication) {
        return "Hello, %s. You are an admin".formatted(authentication.getName());
    }

    @GetMapping("/operator")
    @PreAuthorize("hasRole('OPERATOR')")
    public String operator(Authentication authentication) {
        return "Hello, %s. You are an operator".formatted(authentication.getName());
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String user(Authentication authentication) {
        return "Hello, %s. You are a user".formatted(authentication.getName());
    }
}
