package com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto;

public record ValidateMagicLinkResponse(
        String email,
        String firstName,
        String lastName
) {}
