package com.alertmns.identity.infrastructure.adapter.incoming.web.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RedeemMagicLinkRequest(
        @NotBlank(message = "token must not be blank") String token,
        @NotBlank(message = "password must not be blank") String password
) {}
