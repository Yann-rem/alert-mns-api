package com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto;

public record MeResponse(
        String userId,
        String email,
        String firstName,
        String lastName
) {}
