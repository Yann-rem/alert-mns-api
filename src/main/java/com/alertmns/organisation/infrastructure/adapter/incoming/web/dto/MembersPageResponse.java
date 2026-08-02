package com.alertmns.organisation.infrastructure.adapter.incoming.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Page de membres, accompagnée du total correspondant aux filtres")
public record MembersPageResponse(
        @Schema(description = "Membres de la page courante") List<MemberSummaryResponse> items,
        @Schema(description = "Nombre total de membres correspondant aux filtres, toutes pages confondues")
        long total,
        @Schema(description = "Index de la page courante, à partir de 0") int page,
        @Schema(description = "Taille de page demandée") int size
) {}
