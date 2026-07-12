package com.alertmns.shared.web.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Socle du canal temps réel : configure STOMP over WebSocket, transverse aux Bounded Contexts.
 *
 * <p>Réside dans le <em>shared kernel</em> web ({@code shared.web.ws}) car le transport n'appartient à aucun BC — il
 * est consommé par Alerting (push d'alertes) comme par Messaging (push de messages, typing). L'authentification, elle,
 * reste du ressort du BC Identity : le handshake {@code /ws} est une requête HTTP {@code GET} qui traverse la chaîne
 * de filtres Spring Security et réutilise la <strong>session</strong> (cookie {@code JSESSIONID}) établie au login —
 * aucun JWT, un seul modèle d'auth pour REST et WebSocket.</p>
 *
 * <p>Modèle de routage retenu : <strong>user-destinations</strong> ({@code /user/{id}/queue/...}). Le serveur résout
 * les destinataires (organisation ∪ groupes, cf. « destinataires par requête ») et pousse à chacun, plutôt que de
 * laisser les clients s'abonner librement à des {@code /topic} — pas d'autorisation à gérer au moment du subscribe.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;

    public WebSocketConfig(@Value("${alertmns.websocket.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins.clone();
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
