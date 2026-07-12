package com.alertmns.shared.web.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.security.Principal;
import java.util.Optional;

/**
 * Décorateur qui inscrit / retire chaque session WebSocket dans le {@link WebSocketSessionRegistry} au fil de son cycle
 * de vie, sous la clé {@code userId} (nom du {@code Principal} posé au handshake).
 *
 * <p>C'est le seul endroit où l'on tient une référence à la {@code WebSocketSession} réelle — indispensable pour
 * pouvoir la fermer côté serveur lors d'une révocation.</p>
 */
public class SessionTrackingWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    private final WebSocketSessionRegistry registry;

    public SessionTrackingWebSocketHandlerDecorator(WebSocketHandler delegate, WebSocketSessionRegistry registry) {
        super(delegate);
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userId(session).ifPresent(id -> registry.register(id, session));
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        userId(session).ifPresent(id -> registry.unregister(id, session));
        super.afterConnectionClosed(session, closeStatus);
    }

    private Optional<String> userId(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        return principal == null ? Optional.empty() : Optional.of(principal.getName());
    }
}
