package com.santos.spring_auth.dto.user;

import com.santos.spring_auth.enumeration.Role;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Role role
) {}
