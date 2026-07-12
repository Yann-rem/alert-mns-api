package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaRepository;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration W2 : un message posté via REST est poussé en temps réel aux participants connectés, avec le nom
 * de l'auteur résolu au runtime.
 *
 * <p>Exerce la chaîne complète pour un événement thin : rechargement du message, résolution des destinataires d'une
 * conversation directe (member → userId) et résolution du nom d'affichage de l'auteur (member → user → nom).</p>
 */
@DisplayName("Realtime message push over WebSocket")
class MessageRealtimeIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String ALICE_EMAIL = "alice.msg@alertmns.local";
    private static final String BOB_EMAIL = "bob.msg@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageJpaRepository messageJpaRepository;

    // Le nettoyage parent n'efface pas les messages : on le fait ici (les @AfterEach de sous-classe passent en premier).
    @AfterEach
    void cleanMessages() {
        messageJpaRepository.deleteAll();
    }

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
    @DisplayName("The other participant of a direct conversation receives the posted message with the author name")
    void participantReceivesPostedMessage() throws Exception {
        UserId aliceId = userFactory.registerActive(ALICE_EMAIL, PASSWORD);
        UserId bobId = userFactory.registerActive(BOB_EMAIL, PASSWORD);

        Conversation conversation = Conversation.createDirect(
                OrganisationId.from(TestUserFactory.DEFAULT_ORGANISATION_ID),
                ParticipantPair.of(memberIdOf(aliceId).value(), memberIdOf(bobId).value()),
                Instant.now());
        conversationRepository.save(conversation);

        // Bob connects and subscribes to his personal message queue.
        AuthCookies bob = loginAndAcquireCookies(BOB_EMAIL, PASSWORD);
        StompSession bobSession = connect(bob.session());
        BlockingQueue<Map<String, Object>> received = new LinkedBlockingQueue<>();
        bobSession.subscribe("/user/queue/messages", new StompFrameHandler() {
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
        // SimpleBroker ne rejoue pas : on laisse l'abonnement s'enregistrer avant de poster.
        Thread.sleep(500);

        // Alice posts a message in the conversation.
        AuthCookies alice = loginAndAcquireCookies(ALICE_EMAIL, PASSWORD);
        mutate(
                HttpMethod.POST,
                "/api/messaging/conversations/" + conversation.id().value() + "/messages",
                "{\"content\":\"Bonjour à tous\",\"replyToMessageId\":null}",
                alice
        );

        Map<String, Object> payload = received.poll(5, TimeUnit.SECONDS);
        assertThat(payload).isNotNull();
        assertThat(payload.get("content")).isEqualTo("Bonjour à tous");
        assertThat(payload.get("authorName")).isEqualTo("Test User");

        bobSession.disconnect();
    }
}
