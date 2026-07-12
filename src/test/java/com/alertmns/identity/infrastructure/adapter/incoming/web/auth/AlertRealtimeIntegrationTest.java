package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration W1 : une alerte diffusée via REST est poussée en temps réel au membre destinataire connecté.
 *
 * <p>Exerce toute la chaîne : handshake authentifié (principal = {@code userId}), abonnement à la user-destination
 * {@code /user/queue/alerts}, diffusion {@code POST /api/alerting/alerts} par un admin, résolution des destinataires
 * (organisation), et livraison STOMP au bon utilisateur.</p>
 */
@DisplayName("Realtime alert push over WebSocket")
class AlertRealtimeIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.rt@alertmns.local";
    private static final String MEMBER_EMAIL = "member.rt@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        return client;
    }

    private StompSession connect(String sessionCookie) throws Exception {
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Cookie", sessionCookie);
        return stompClient()
                .connectAsync("ws://localhost:" + port + "/ws", handshakeHeaders, new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("A connected member receives an organisation-wide alert broadcast by an admin")
    void memberReceivesOrganisationAlert() throws Exception {
        userFactory.registerActiveAdmin(ADMIN_EMAIL, PASSWORD);
        userFactory.registerActive(MEMBER_EMAIL, PASSWORD);

        // The member connects and subscribes to its personal alert queue.
        AuthCookies member = loginAndAcquireCookies(MEMBER_EMAIL, PASSWORD);
        StompSession memberSession = connect(member.session());

        BlockingQueue<Map<String, Object>> received = new LinkedBlockingQueue<>();
        memberSession.subscribe("/user/queue/alerts", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((Map<String, Object>) payload);
            }
        });
        // SimpleBroker ne rejoue pas : on laisse l'abonnement s'enregistrer avant de diffuser.
        Thread.sleep(500);

        // The admin broadcasts an organisation-wide alert.
        AuthCookies admin = loginAndAcquireCookies(ADMIN_EMAIL, PASSWORD);
        mutate(
                HttpMethod.POST,
                "/api/alerting/alerts",
                "{\"content\":\"Évacuation générale\",\"level\":\"URGENT\",\"audienceKind\":\"ORGANISATION\",\"groupId\":null}",
                admin
        );

        Map<String, Object> payload = received.poll(5, TimeUnit.SECONDS);
        assertThat(payload).isNotNull();
        assertThat(payload.get("content")).isEqualTo("Évacuation générale");
        assertThat(payload.get("level")).isEqualTo("URGENT");
        assertThat(payload.get("audienceKind")).isEqualTo("ORGANISATION");

        memberSession.disconnect();
    }
}
