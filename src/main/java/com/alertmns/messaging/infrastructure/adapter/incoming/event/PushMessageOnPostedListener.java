package com.alertmns.messaging.infrastructure.adapter.incoming.event;

import com.alertmns.messaging.application.DispatchMessageService;
import com.alertmns.messaging.domain.event.MessagePosted;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui pousse un message en temps réel aux participants de la conversation dès qu'il est posté.
 *
 * <p>Réagit à {@link MessagePosted} en déléguant à {@link DispatchMessageService}. Synchrone : la livraison réseau aux
 * clients est déjà asynchrone (canal sortant STOMP), le seul coût sur le thread du POST est la résolution des
 * destinataires et du nom de l'auteur.</p>
 */
public final class PushMessageOnPostedListener {

    private final DispatchMessageService dispatchMessageService;

    public PushMessageOnPostedListener(DispatchMessageService dispatchMessageService) {
        this.dispatchMessageService = Objects.requireNonNull(
                dispatchMessageService, "dispatchMessageService must not be null");
    }

    @EventListener
    public void onMessagePosted(MessagePosted event) {
        dispatchMessageService.dispatch(event);
    }
}
