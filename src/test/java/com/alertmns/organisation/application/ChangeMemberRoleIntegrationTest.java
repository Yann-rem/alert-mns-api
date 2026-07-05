package com.alertmns.organisation.application;

import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.organisation.domain.exception.LastAdminCannotBeRemovedException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.ChangeMemberRoleUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ChangeMemberRoleCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test d'intégration du changement de rôle via {@link ChangeMemberRoleUseCase}.
 *
 * <p>Vérifie, sur une base réelle, la promotion d'un membre vers MANAGER et le respect de l'invariant ADR-0013
 * (« au moins un ADMIN actif ») lors d'une rétrogradation.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Change member role (use case integration)")
class ChangeMemberRoleIntegrationTest {

    private static final OrganisationId ORGANISATION_ID =
            OrganisationId.from(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final Instant NOW = Instant.parse("2026-06-20T10:00:00Z");

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @MockitoBean
    private MailerPort mailer;

    @Autowired
    private ChangeMemberRoleUseCase changeMemberRoleUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @AfterEach
    void cleanDatabase() {
        memberJpaRepository.deleteAll();
    }

    private ChangeMemberRoleCommand command(MemberId memberId, String role) {
        return new ChangeMemberRoleCommand(ORGANISATION_ID.value().toString(), memberId.value().toString(), role);
    }

    @Test
    @DisplayName("promotes a member to MANAGER")
    void promotesToManager() {
        MemberId id = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                id, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW));

        changeMemberRoleUseCase.changeRole(command(id, "MANAGER"));

        assertThat(memberRepository.findById(id).orElseThrow().role()).isEqualTo(MemberRole.MANAGER);
    }

    @Test
    @DisplayName("refuses to demote the last active admin and leaves the role unchanged")
    void refusesDemotingLastAdmin() {
        MemberId id = MemberId.generate();
        memberRepository.save(Member.reconstitute(
                id, ORGANISATION_ID, UUID.randomUUID(), MemberRole.ADMIN, MemberStatus.ACTIVE, NOW));

        assertThatThrownBy(() -> changeMemberRoleUseCase.changeRole(command(id, "MEMBER")))
                .isInstanceOf(LastAdminCannotBeRemovedException.class);

        assertThat(memberRepository.findById(id).orElseThrow().role()).isEqualTo(MemberRole.ADMIN);
    }
}
