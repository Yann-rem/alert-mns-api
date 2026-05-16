package com.alertmns.iam.infrastructure.adapter.incoming.web.auth.dto;

public record ValidateMagicLinkResponse(
        String email,
        String firstName,
        String lastName
) {}
