package com.santos.spring_auth.dto.user;

import com.santos.spring_auth.enumeration.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequestDTO(
        @NotBlank @Size(min = 3, max = 60) String username,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotNull Role role
) {}
