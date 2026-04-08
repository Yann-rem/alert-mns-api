package com.alertmns.iam.infrastructure.adapter.incoming.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String rawPassword,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName
) {}
