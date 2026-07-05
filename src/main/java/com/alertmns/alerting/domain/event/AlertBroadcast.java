package com.alertmns.alerting.domain.event;

import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.UUID;

/**
 * Événement de domaine représentant la diffusion d'une alerte.
 *
 * <p>Thick event volontairement self-contained : il embarque l'audience, le niveau et le contenu afin qu'un
 * consommateur (le push temps réel du chantier WebSocket/SSE, ou un futur BC Notification) puisse router et afficher
 * la notification sans recharger l'agrégat émetteur.</p>
 */
public record AlertBroadcast(
        AlertId alertId,
        OrganisationId organisationId,
        UUID issuerId,
        AlertContent content,
        AlertAudience audience,
        AlertLevel level,
        Instant occurredOn
) implements DomainEvent {}
