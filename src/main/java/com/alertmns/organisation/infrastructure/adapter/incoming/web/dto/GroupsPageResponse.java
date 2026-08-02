package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Page de groupes, accompagnée du total correspondant au filtre")
public record GroupsPageResponse(
        @Schema(description = "Groupes de la page courante") List<GroupSummaryResponse> items,
        @Schema(description = "Nombre total de groupes correspondant au filtre") long total,
        @Schema(description = "Index de la page courante, à partir de 0") int page,
        @Schema(description = "Taille de page demandée") int size
) {}
