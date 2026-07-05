package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.AlertAudienceKind;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.incoming.BroadcastAlertUseCase;
import com.alertmns.alerting.domain.port.incoming.command.BroadcastAlertCommand;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaEntity;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaRepository;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
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
 * Test d'intégration de la diffusion d'alerte via {@link BroadcastAlertUseCase}.
 *
 * <p>Vérifie, sur une base réelle, qu'une alerte diffusée par le membre courant est persistée avec son émetteur, son
 * audience et son niveau. L'autorisation par rôle est couverte séparément par {@code AlertAuthorizationIntegrationTest}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Broadcast alert (use case integration)")
class BroadcastAlertIntegrationTest {

    private static final OrganisationId ORGANISATION_ID =
            OrganisationId.from(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final Instant NOW = Instant.parse("2026-06-15T08:00:00Z");

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
    private BroadcastAlertUseCase broadcastAlertUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AlertJpaRepository alertJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberId memberId;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        memberId = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                memberId, ORGANISATION_ID, userId, MemberRole.ADMIN, MemberStatus.ACTIVE, NOW));
        when(currentUserPort.currentUser())
                .thenReturn(Optional.of(new AuthenticatedUser(UserId.from(userId))));
    }

    @AfterEach
    void cleanDatabase() {
        alertJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("persists an organisation-wide alert issued by the current member")
    void persistsOrganisationWideAlert() {
        AlertId id = broadcastAlertUseCase.broadcast(
                new BroadcastAlertCommand("Fermeture exceptionnelle demain", "URGENT", "ORGANISATION", null));

        AlertJpaEntity saved = alertJpaRepository.findById(id.value()).orElseThrow();
        assertThat(saved.getOrganisationId()).isEqualTo(ORGANISATION_ID.value());
        assertThat(saved.getIssuerId()).isEqualTo(memberId.value());
        assertThat(saved.getContent()).isEqualTo("Fermeture exceptionnelle demain");
        assertThat(saved.getAudienceKind()).isEqualTo(AlertAudienceKind.ORGANISATION);
        assertThat(saved.getGroupId()).isNull();
        assertThat(saved.getLevel()).isEqualTo(AlertLevel.URGENT);
    }

    @Test
    @DisplayName("persists a group-targeted alert")
    void persistsGroupAlert() {
        UUID groupId = UUID.randomUUID();

        AlertId id = broadcastAlertUseCase.broadcast(
                new BroadcastAlertCommand("Cours annulé", "INFO", "GROUP", groupId.toString()));

        AlertJpaEntity saved = alertJpaRepository.findById(id.value()).orElseThrow();
        assertThat(saved.getAudienceKind()).isEqualTo(AlertAudienceKind.GROUP);
        assertThat(saved.getGroupId()).isEqualTo(groupId);
        assertThat(saved.getLevel()).isEqualTo(AlertLevel.INFO);
    }
}
