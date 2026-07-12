package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests d'intégration du socle WebSocket (W0) : le handshake {@code /ws} réutilise l'authentification par session.
 *
 * <p>Confirme le choix d'architecture « pas de JWT » : un client STOMP qui présente le cookie {@code JSESSIONID}
 * établi au login est accepté, un client anonyme est rejeté par la chaîne de filtres Spring Security avant même
 * l'upgrade WebSocket.</p>
 */
@DisplayName("WebSocket handshake auth on /ws")
class WebSocketHandshakeAuthIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String USER_EMAIL = "ws.user@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new StringMessageConverter());
        return client;
    }

    private String wsUrl() {
        return "ws://localhost:" + port + "/ws";
    }

    @Test
    @DisplayName("Authenticated handshake (session cookie) establishes the STOMP session")
    void shouldConnectWhenAuthenticated() throws Exception {
        userFactory.registerActive(USER_EMAIL, PASSWORD);
        AuthCookies cookies = loginAndAcquireCookies(USER_EMAIL, PASSWORD);

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Cookie", cookies.session());

        StompSession session = stompClient()
                .connectAsync(wsUrl(), handshakeHeaders, new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isTrue();
        session.disconnect();
    }

    @Test
    @DisplayName("Anonymous handshake (no session cookie) is rejected before the upgrade")
    void shouldRejectWhenAnonymous() {
        assertThatThrownBy(() -> stompClient()
                .connectAsync(wsUrl(), new WebSocketHttpHeaders(), new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);
    }
}
