package com.alertmns.alerting.infrastructure.adapter.incoming.web;

import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.port.incoming.BroadcastAlertUseCase;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter web — diffusion d'alertes. Réservé aux rôles {@code ADMIN} et {@code MANAGER} : l'autorisation par rôle
 * (RBAC) est placée sur l'adapter entrant pour garder l'application et le domaine agnostiques de Spring Security. Les
 * rôles sont dérivés du {@code Member.role} du BC Organisation.
 */
@RestController
@RequestMapping("/api/alerting/alerts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
@Tag(name = "Alerts", description = "Diffusion d'alertes")
public class AlertController {

    private final BroadcastAlertUseCase broadcastAlertUseCase;

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
    public BroadcastAlertResponse broadcast(@Valid @RequestBody BroadcastAlertRequest request) {
        AlertId id = broadcastAlertUseCase.broadcast(AlertWebMapper.toBroadcastAlertCommand(request));
        return new BroadcastAlertResponse(id.value());
    }
}
