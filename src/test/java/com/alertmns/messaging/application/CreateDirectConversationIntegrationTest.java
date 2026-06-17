package com.alertmns.messaging.application;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.CreateDirectConversationUseCase;
import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaEntity;
import com.alertmns.messaging.infrastructure.adapter.outgoing.persistence.ConversationJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test d'intégration de la création d'une conversation directe via {@link CreateDirectConversationUseCase}.
 *
 * <p>Exerce de bout en bout la persistance DIRECT : résolution de l'initiateur (current user mocké → Member),
 * écriture (mapper {@code toEntity}) puis relecture par la paire (mapper {@code toDomain}) lors du second appel
 * idempotent.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Create direct conversation (use case integration)")
class CreateDirectConversationIntegrationTest {

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
    private CreateDirectConversationUseCase createDirectConversationUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ConversationJpaRepository conversationJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberId initiatorId;
    private MemberId targetId;

    @BeforeEach
    void setUp() {
        UUID initiatorUserId = UUID.randomUUID();
        initiatorId = MemberId.generate();
        targetId = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                initiatorId, ORGANISATION_ID, initiatorUserId, MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));
        memberRepository.save(Member.reconstitute(
                targetId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));
        when(currentUserPort.currentUser()).thenReturn(Optional.of(new AuthenticatedUser(UserId.from(initiatorUserId))));
    }

    @AfterEach
    void cleanDatabase() {
        conversationJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("creates a DIRECT conversation with canonically ordered participants and no group/name")
    void shouldCreateDirectConversation() {
        ConversationId id = createDirectConversationUseCase.create(
                new CreateDirectConversationCommand(targetId.value().toString()));

        ConversationJpaEntity conversation = conversationJpaRepository.findById(id.value()).orElseThrow();
        ParticipantPair expected = ParticipantPair.of(initiatorId.value(), targetId.value());
        assertThat(conversation.getKind()).isEqualTo(ConversationKind.DIRECT);
        assertThat(conversation.getOrganisationId()).isEqualTo(ORGANISATION_ID.value());
        assertThat(conversation.getParticipantLow()).isEqualTo(expected.low());
        assertThat(conversation.getParticipantHigh()).isEqualTo(expected.high());
        assertThat(conversation.getGroupId()).isNull();
        assertThat(conversation.getName()).isNull();
    }

    @Test
    @DisplayName("is idempotent: a second call returns the same conversation without creating a duplicate")
    void shouldBeIdempotent() {
        ConversationId first = createDirectConversationUseCase.create(
                new CreateDirectConversationCommand(targetId.value().toString()));
        ConversationId second = createDirectConversationUseCase.create(
                new CreateDirectConversationCommand(targetId.value().toString()));

        assertThat(second).isEqualTo(first);
        assertThat(conversationJpaRepository.findAll()).hasSize(1);
    }
}
