package com.alertmns.identity.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.event.UserAnonymized;
import com.alertmns.identity.domain.event.UserSuspended;
import com.alertmns.identity.infrastructure.adapter.incoming.web.security.DomainUserDetails;
import com.alertmns.shared.web.ws.WebSocketSessionRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

import java.util.List;
import java.util.Objects;

/**
 * Ferme les sessions WebSocket ouvertes d'un utilisateur dès qu'il ne doit plus être connecté.
 *
 * <p>Prolonge la « révocation immédiate » d'ADR-0019 au canal temps réel : un socket déjà ouvert ne se referme pas
 * quand la session HTTP est invalidée. Trois déclencheurs :</p>
 * <ul>
 *   <li>{@link HttpSessionDestroyedEvent} — logout ou expiration de la session HTTP ;</li>
 *   <li>{@link UserSuspended} — suspension du compte (ADR-0019) ;</li>
 *   <li>{@link UserAnonymized} — anonymisation RGPD (le compte devient inerte, ses sockets aussi).</li>
 * </ul>
 *
 * <p>La politique de « quand révoquer » relève du BC Identity (auth) ; le « comment fermer » est délégué au
 * {@link WebSocketSessionRegistry} du shared kernel.</p>
 */
public final class WebSocketRevocationListener {

    private final WebSocketSessionRegistry sessionRegistry;

    public WebSocketRevocationListener(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry must not be null");
    }

    @EventListener
    public void onUserSuspended(UserSuspended event) {
        sessionRegistry.closeSessionsOf(event.userId().value().toString());
    }

    @EventListener
    public void onUserAnonymized(UserAnonymized event) {
        sessionRegistry.closeSessionsOf(event.userId().value().toString());
    }

    @EventListener
    public void onHttpSessionDestroyed(HttpSessionDestroyedEvent event) {
        revoke(event.getSecurityContexts());
    }

    void revoke(List<SecurityContext> securityContexts) {
        for (SecurityContext context : securityContexts) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof DomainUserDetails details) {
                sessionRegistry.closeSessionsOf(details.userId().value().toString());
            }
        }
    }
}
