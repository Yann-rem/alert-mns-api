package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration W4 : suspendre un utilisateur connecté ferme son socket WebSocket (révocation immédiate,
 * prolongement d'ADR-0019 au canal temps réel).
 */
@DisplayName("WebSocket revocation on suspend")
class WebSocketRevocationIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.revoke@alertmns.local";
    private static final String MEMBER_EMAIL = "member.revoke@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @LocalServerPort
    private int port;

    private StompSession connect(String sessionCookie) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new StringMessageConverter());
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Cookie", sessionCookie);
        return client
                .connectAsync("ws://localhost:" + port + "/ws", handshakeHeaders, new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Suspending a connected user closes their open socket")
    void shouldCloseSocketWhenUserIsSuspended() throws Exception {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        UserId memberUserId = userFactory.registerActive(MEMBER_EMAIL, PASSWORD);

        AuthCookies member = loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);
        StompSession memberSession = connect(member.session());
        assertThat(memberSession.isConnected()).isTrue();
        // Laisse le décorateur enregistrer la session avant la révocation.
        Thread.sleep(500);

        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
        ResponseEntity<String> response = mutate(
                HttpMethod.POST, "/api/users/" + memberUserId.value() + "/suspend", null, admin);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Le serveur ferme le socket du membre suspendu ; on attend que le client le constate.
        long deadline = System.currentTimeMillis() + 5000;
        while (memberSession.isConnected() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertThat(memberSession.isConnected()).isFalse();
    }
}
