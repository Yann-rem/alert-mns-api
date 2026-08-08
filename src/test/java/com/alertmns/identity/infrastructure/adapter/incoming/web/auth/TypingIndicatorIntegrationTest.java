package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration W3 : le signal « en train d'écrire » émis par un participant est relayé à l'autre en temps réel.
 *
 * <p>Premier flux <strong>bidirectionnel</strong> exercé de bout en bout : Bob envoie un {@code SEND} STOMP sur
 * {@code /app/conversations/{id}/typing}, Alice (abonnée à {@code /user/queue/typing}) reçoit le signal portant le
 * {@code userId} de Bob.</p>
 */
@DisplayName("Typing indicator over WebSocket")
class TypingIndicatorIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ALICE_EMAIL = "alice.typing@alertmns.local";
    private static final String BOB_EMAIL = "bob.typing@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private StompSession connect(String sessionCookie) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Cookie", sessionCookie);
        return client
                .connectAsync("ws://localhost:" + port + "/ws", handshakeHeaders, new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);
    }

    private MemberId memberIdOf(UserId userId) {
        return memberRepository.findByUserId(userId.value()).orElseThrow().id();
    }

    @Test
    @DisplayName("Alice receives that Bob is typing in their conversation")
    void aliceReceivesBobTyping() throws Exception {
        UserId aliceId = userFactory.registerActive(ALICE_EMAIL, PASSWORD);
        UserId bobId = userFactory.registerActive(BOB_EMAIL, PASSWORD);

        Conversation conversation = Conversation.createDirect(
                OrganisationId.from(TestUserFactory.DEFAULT_ORGANISATION_ID),
                ParticipantPair.of(memberIdOf(aliceId).value(), memberIdOf(bobId).value()),
                Instant.now());
        conversationRepository.save(conversation);

        AuthCookies alice = loginAndAcquireCookies(ALICE_EMAIL, PASSWORD);
        StompSession aliceSession = connect(alice.session());
        BlockingQueue<Map<String, Object>> received = new LinkedBlockingQueue<>();
        aliceSession.subscribe("/user/queue/typing", new StompFrameHandler() {
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

        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);
        StompSession bobSession = connect(bob.session());
        // SimpleBroker ne rejoue pas : on laisse l'abonnement d'Alice s'enregistrer avant l'envoi de Bob.
        Thread.sleep(500);

        bobSession.send("/app/conversations/" + conversation.id().value() + "/typing", Map.of());

        Map<String, Object> payload = received.poll(5, TimeUnit.SECONDS);
        assertThat(payload).isNotNull();
        assertThat(payload.get("conversationId")).isEqualTo(conversation.id().value().toString());
        assertThat(payload.get("userId")).isEqualTo(bobId.value().toString());
        // Le nom voyage avec le signal : le client ne sait pas traduire un userId.
        assertThat(payload.get("userName")).isEqualTo("Test User");

        aliceSession.disconnect();
        bobSession.disconnect();
    }
}
