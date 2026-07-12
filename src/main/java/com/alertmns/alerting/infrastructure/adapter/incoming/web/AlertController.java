package com.alertmns.alerting.infrastructure.adapter.incoming.web;

import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.port.incoming.BroadcastAlertUseCase;
import com.alertmns.alerting.domain.port.incoming.ListMyAlertsUseCase;
import com.alertmns.alerting.infrastructure.adapter.incoming.web.dto.AlertResponse;
import com.alertmns.alerting.infrastructure.adapter.incoming.web.dto.BroadcastAlertRequest;
import com.alertmns.alerting.infrastructure.adapter.incoming.web.dto.BroadcastAlertResponse;
import com.alertmns.alerting.infrastructure.adapter.incoming.web.mapper.AlertWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapter web des alertes.
 *
 * <p><b>Diffusion</b> ({@code POST}) : réservée aux rôles {@code ADMIN} et {@code MANAGER} (garde de méthode).
 * <b>Lecture</b> ({@code GET}) : ouverte à tout membre authentifié — chacun voit les alertes qui le concernent
 * (organisation + ses groupes). Le RBAC reste sur l'adapter entrant ; l'application et le domaine restent agnostiques
 * de Spring Security.</p>
 */
@RestController
@RequestMapping("/api/alerting/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Diffusion et consultation des alertes")
public class AlertController {

    private final BroadcastAlertUseCase broadcastAlertUseCase;
    private final ListMyAlertsUseCase listMyAlertsUseCase;

    @Operation(
            summary = "Diffuser une alerte",
            description = "Diffuse une alerte à toute l'organisation ou à un groupe. Réservé aux rôles ADMIN et MANAGER.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Alerte diffusée"),
                    @ApiResponse(responseCode = "400", description = "Données invalides (contenu, niveau, audience ou groupId)"),
                    @ApiResponse(responseCode = "403", description = "Rôle insuffisant")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public BroadcastAlertResponse broadcast(@Valid @RequestBody BroadcastAlertRequest request) {
        AlertId id = broadcastAlertUseCase.broadcast(AlertWebMapper.toBroadcastAlertCommand(request));
        return new BroadcastAlertResponse(id.value());
    }

    @Operation(
            summary = "Lister mes alertes",
            description = "Retourne les alertes destinées à l'utilisateur courant (organisation et ses groupes), de la plus récente à la plus ancienne.",
            responses = @ApiResponse(responseCode = "200", description = "Liste des alertes")
    )
    @GetMapping
    public List<AlertResponse> listMine() {
        return listMyAlertsUseCase.list().stream()
                .map(AlertWebMapper::toAlertResponse)
                .toList();
    }
}
