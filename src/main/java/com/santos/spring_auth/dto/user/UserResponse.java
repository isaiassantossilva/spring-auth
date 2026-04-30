package com.santos.spring_auth.dto.user;

import com.santos.spring_auth.enumeration.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role
) {}
