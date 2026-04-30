package com.santos.spring_auth.dto.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description
) {}
