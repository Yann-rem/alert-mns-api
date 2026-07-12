package com.alertmns.alerting.infrastructure.adapter.incoming.event;

import com.alertmns.alerting.application.DispatchAlertService;
import com.alertmns.alerting.domain.event.AlertBroadcast;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui pousse une alerte en temps réel à ses destinataires dès qu'elle est diffusée.
 *
 * <p>Réagit à {@link AlertBroadcast} en déléguant à {@link DispatchAlertService}. L'événement est publié <em>après</em>
 * le commit du {@code save()} (les use cases publient hors transaction), donc un listener synchrone observe déjà une
 * alerte persistée.</p>
 *
 * <p>Volontairement synchrone : {@code convertAndSendToUser} ne fait qu'enfiler dans le canal sortant STOMP (qui
 * dispose de son propre pool de threads) — la livraison réseau aux clients est donc déjà asynchrone par construction,
 * et le seul coût porté par le thread du POST est la résolution des destinataires.</p>
 */
public final class PushAlertOnBroadcastListener {

    private final DispatchAlertService dispatchAlertService;

    public PushAlertOnBroadcastListener(DispatchAlertService dispatchAlertService) {
        this.dispatchAlertService = Objects.requireNonNull(
                dispatchAlertService, "dispatchAlertService must not be null");
    }

    @EventListener
    public void onAlertBroadcast(AlertBroadcast event) {
        dispatchAlertService.dispatch(event);
    }
}
