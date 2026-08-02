package com.alertmns.messaging.application;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.MessageView;
import com.alertmns.messaging.domain.port.incoming.ReadConversationMessagesUseCase;
import com.alertmns.messaging.domain.port.incoming.command.ReadConversationMessagesQuery;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.MessageJpaRepository;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.shared.AuthenticatedUser;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Test d'intégration de la lecture paginée des messages via {@link ReadConversationMessagesUseCase}.
 *
 * <p>Exerce, sur une base réelle : le contrôle d'accès (paire d'un DM, appartenance au groupe via l'ACL),
 * le tri décroissant par {@code sentAt} (plus récents d'abord) et la pagination.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Read conversation messages (use case integration)")
class ReadConversationMessagesIntegrationTest {

    private static final OrganisationId ORGANISATION_ID =
            OrganisationId.from(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final Instant NOW = Instant.parse("2026-06-01T10:00:00Z");

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @MockitoBean
    private MailerPort mailer;

    @MockitoBean
    private CurrentUserPort currentUserPort;

    @Autowired
    private ReadConversationMessagesUseCase readConversationMessagesUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationJpaRepository conversationJpaRepository;

    @Autowired
    private MessageJpaRepository messageJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberId readerId;

    @BeforeEach
    void setUp() {
        UUID readerUserId = UUID.randomUUID();
        readerId = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                readerId, ORGANISATION_ID, readerUserId, MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));
        when(currentUserPort.currentUser())
                .thenReturn(Optional.of(new AuthenticatedUser(UserId.from(readerUserId))));
    }

    @AfterEach
    void cleanDatabase() {
        messageJpaRepository.deleteAll();
        conversationJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    private Conversation directConversationWithReader() {
        ParticipantPair pair = ParticipantPair.of(readerId.value(), UUID.randomUUID());
        Conversation conversation = Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, null, null, ConversationKind.DIRECT, pair, NOW);
        conversationRepository.save(conversation);
        return conversation;
    }

    private void seedMessage(ConversationId conversationId, MessageId id, String content, Instant sentAt) {
        messageRepository.save(Message.reconstitute(
                id, conversationId, readerId.value(), MessageContent.of(content), null, sentAt));
    }

    @Test
    @DisplayName("returns the messages of a conversation ordered most recent first")
    void shouldReturnMessagesMostRecentFirst() {
        Conversation conversation = directConversationWithReader();
        MessageId older = MessageId.generate();
        MessageId middle = MessageId.generate();
        MessageId newer = MessageId.generate();
        seedMessage(conversation.id(), older, "1", NOW);
        seedMessage(conversation.id(), middle, "2", NOW.plusSeconds(60));
        seedMessage(conversation.id(), newer, "3", NOW.plusSeconds(120));

        List<MessageView> result = readConversationMessagesUseCase.read(
                new ReadConversationMessagesQuery(conversation.id().value().toString(), 0, 50));

        assertThat(result).extracting(view -> view.message().id()).containsExactly(newer, middle, older);
    }

    @Test
    @DisplayName("paginates the messages")
    void shouldPaginate() {
        Conversation conversation = directConversationWithReader();
        MessageId older = MessageId.generate();
        MessageId middle = MessageId.generate();
        MessageId newer = MessageId.generate();
        seedMessage(conversation.id(), older, "1", NOW);
        seedMessage(conversation.id(), middle, "2", NOW.plusSeconds(60));
        seedMessage(conversation.id(), newer, "3", NOW.plusSeconds(120));

        List<MessageView> firstPage = readConversationMessagesUseCase.read(
                new ReadConversationMessagesQuery(conversation.id().value().toString(), 0, 2));
        List<MessageView> secondPage = readConversationMessagesUseCase.read(
                new ReadConversationMessagesQuery(conversation.id().value().toString(), 1, 2));

        assertThat(firstPage).extracting(view -> view.message().id()).containsExactly(newer, middle);
        assertThat(secondPage).extracting(view -> view.message().id()).containsExactly(older);
    }

    @Test
    @DisplayName("rejects reading a GROUP conversation when the reader is not a member of the group")
    void shouldRejectWhenNotGroupMember() {
        UUID groupId = UUID.randomUUID();
        Conversation conversation = Conversation.reconstitute(
                ConversationId.generate(), ORGANISATION_ID, groupId, ConversationName.of("Privé"),
                ConversationKind.GROUP, null, NOW);
        conversationRepository.save(conversation);

        assertThatThrownBy(() -> readConversationMessagesUseCase.read(
                new ReadConversationMessagesQuery(conversation.id().value().toString(), 0, 50)))
                .isInstanceOf(NotAConversationParticipantException.class);
    }
}
