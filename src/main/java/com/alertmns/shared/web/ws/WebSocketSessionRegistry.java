package com.alertmns.shared.web.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre en mémoire des sessions WebSocket ouvertes, indexées par {@code userId}.
 *
 * <p>Permet de <strong>fermer activement</strong> les sockets d'un utilisateur lorsqu'il n'a plus le droit d'être
 * connecté (logout, suspension, anonymisation) — un socket déjà ouvert ne se ferme pas tout seul quand la session HTTP
 * est invalidée. Alimenté par {@link SessionTrackingWebSocketHandlerDecorator}. Mono-instance : suffisant pour le
 * monolithe (un scale-out multi-instances passerait par un registre distribué, sans changer ce contrat).</p>
 */
public class WebSocketSessionRegistry {

    /** Code de fermeture applicatif signalant une révocation (le client peut le distinguer d'une coupure réseau). */
    static final CloseStatus SESSION_REVOKED = new CloseStatus(4001, "session revoked");

    private final Map<String, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public void register(String userId, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(String userId, WebSocketSession session) {
        sessionsByUser.computeIfPresent(userId, (key, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    /**
     * Ferme toutes les sessions ouvertes de l'utilisateur et les retire du registre.
     *
     * @param userId l'identifiant de l'utilisateur dont les sockets doivent être révoqués
     */
    public void closeSessionsOf(String userId) {
        Set<WebSocketSession> sessions = sessionsByUser.remove(userId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            closeQuietly(session);
        }
    }

    public boolean hasSessions(String userId) {
        return sessionsByUser.containsKey(userId);
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            if (session.isOpen()) {
                session.close(SESSION_REVOKED);
            }
        } catch (IOException ignored) {
            // Socket déjà rompu côté client : rien à faire.
        }
    }
}
