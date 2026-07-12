package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.incoming.ListMyAlertsUseCase;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.infrastructure.adapter.outgoing.persistence.AlertJpaRepository;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipJpaRepository;
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
import static org.mockito.Mockito.when;

/**
 * Test d'intégration du listage des alertes de l'utilisateur courant via {@link ListMyAlertsUseCase}.
 *
 * <p>Vérifie, sur une base réelle, qu'on retourne les alertes de son organisation et celles de ses groupes (via
 * l'ACL), triées du plus récent au plus ancien, et qu'on exclut les alertes qui ne le concernent pas.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("List my alerts (use case integration)")
class ListMyAlertsIntegrationTest {

    private static final OrganisationId ORGANISATION_ID =
            OrganisationId.from(UUID.fromString("00000000-0000-0000-0000-000000000003"));
    private static final Instant NOW = Instant.parse("2026-06-25T10:00:00Z");

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
    private ListMyAlertsUseCase listMyAlertsUseCase;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private AlertJpaRepository alertJpaRepository;

    @Autowired
    private GroupMembershipJpaRepository groupMembershipJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private MemberId memberId;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        memberId = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                memberId, ORGANISATION_ID, userId, MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));
        when(currentUserPort.currentUser())
                .thenReturn(Optional.of(new AuthenticatedUser(UserId.from(userId))));
    }

    @AfterEach
    void cleanDatabase() {
        alertJpaRepository.deleteAll();
        groupMembershipJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    private Alert organisationAlert(OrganisationId organisationId, Instant issuedAt) {
        return Alert.reconstitute(
                AlertId.generate(), organisationId, UUID.randomUUID(),
                AlertContent.of("Org"), AlertAudience.organisation(), AlertLevel.INFO, issuedAt);
    }

    private Alert groupAlert(UUID groupId, Instant issuedAt) {
        return Alert.reconstitute(
                AlertId.generate(), ORGANISATION_ID, UUID.randomUUID(),
                AlertContent.of("Group"), AlertAudience.group(groupId), AlertLevel.URGENT, issuedAt);
    }

    @Test
    @DisplayName("returns the member's organisation and group alerts, most recent first, excluding others")
    void returnsMyAlerts() {
        Alert myOrgAlert = organisationAlert(ORGANISATION_ID, NOW);
        alertRepository.save(myOrgAlert);

        UUID groupId = UUID.randomUUID();
        groupMembershipRepository.save(GroupMembership.reconstitute(
                GroupMembershipId.from(UUID.randomUUID()), ORGANISATION_ID, GroupId.from(groupId), memberId, NOW));
        Alert myGroupAlert = groupAlert(groupId, NOW.plusSeconds(120));
        alertRepository.save(myGroupAlert);

        // an organisation-wide alert of another organisation → must be excluded
        alertRepository.save(organisationAlert(OrganisationId.from(UUID.randomUUID()), NOW));
        // a group alert for a group the member is not in → must be excluded
        alertRepository.save(groupAlert(UUID.randomUUID(), NOW));

        List<Alert> result = listMyAlertsUseCase.list();

        assertThat(result).extracting(Alert::id).containsExactly(myGroupAlert.id(), myOrgAlert.id());
    }

    @Test
    @DisplayName("returns an empty list when there is no alert for the member")
    void returnsEmptyWhenNone() {
        assertThat(listMyAlertsUseCase.list()).isEmpty();
    }
}
