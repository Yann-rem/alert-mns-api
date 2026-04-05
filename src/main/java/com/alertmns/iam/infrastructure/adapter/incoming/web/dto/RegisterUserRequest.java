package com.alertmns.iam.infrastructure.adapter.incoming.web.dto;

public record RegisterUserRequest(
        String email,
        String rawPassword,
        String firstName,
        String lastName
) {}
