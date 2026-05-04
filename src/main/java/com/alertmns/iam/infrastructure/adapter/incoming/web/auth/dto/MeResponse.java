package com.alertmns.iam.infrastructure.adapter.incoming.web.auth.dto;

public record MeResponse(
        String userId,
        String organisationId,
        String email,
        String firstName,
        String lastName,
        String role
) {}
