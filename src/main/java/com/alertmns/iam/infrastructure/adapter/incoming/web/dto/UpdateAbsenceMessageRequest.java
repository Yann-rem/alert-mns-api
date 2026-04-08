package com.alertmns.iam.infrastructure.adapter.incoming.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAbsenceMessageRequest(
        @NotBlank @Size(max = 500) String content,
        boolean active
) {}
