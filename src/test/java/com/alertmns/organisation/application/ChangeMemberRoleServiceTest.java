package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.LastAdminCannotBeRemovedException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.command.ChangeMemberRoleCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ChangeMemberRoleService")
@ExtendWith(MockitoExtension.class)
class ChangeMemberRoleServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-06-20T10:00:00Z");
    static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    MemberRepository repository;

    @Mock
    EventPublisher publisher;

    ChangeMemberRoleService service;

    @BeforeEach
    void setUp() {
        service = new ChangeMemberRoleService(repository, publisher, CLOCK);
    }

    private ChangeMemberRoleCommand command(MemberId memberId, String role) {
        return new ChangeMemberRoleCommand(
                ORGANISATION_ID.value().toString(), memberId.value().toString(), role);
    }

    private Member memberWith(MemberId id, MemberRole role, MemberStatus status) {
        return Member.reconstitute(id, ORGANISATION_ID, UUID.randomUUID(), role, status, NOW);
    }

    @Nested
    @DisplayName("Change")
    class Change {

        @Test
        @DisplayName("should promote a MEMBER to MANAGER and publish")
        void promotesToManager() {
            MemberId id = MemberId.generate();
            when(repository.findById(id))
                    .thenReturn(Optional.of(memberWith(id, MemberRole.MEMBER, MemberStatus.ACTIVE)));

            service.changeRole(command(id, "MANAGER"));

            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(repository).save(captor.capture());
            assertEquals(MemberRole.MANAGER, captor.getValue().role());
            verify(publisher).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Last-admin invariant")
    class LastAdmin {

        @Test
        @DisplayName("should refuse demoting the last active admin")
        void refusesDemotingLastAdmin() {
            MemberId id = MemberId.generate();
            when(repository.findById(id))
                    .thenReturn(Optional.of(memberWith(id, MemberRole.ADMIN, MemberStatus.ACTIVE)));
            when(repository.countByOrganisationIdAndRoleAndStatus(
                    ORGANISATION_ID, MemberRole.ADMIN, MemberStatus.ACTIVE)).thenReturn(1L);

            assertThrows(LastAdminCannotBeRemovedException.class, () -> service.changeRole(command(id, "MEMBER")));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should allow demoting an admin when others remain")
        void allowsDemotingWhenOthersRemain() {
            MemberId id = MemberId.generate();
            when(repository.findById(id))
                    .thenReturn(Optional.of(memberWith(id, MemberRole.ADMIN, MemberStatus.ACTIVE)));
            when(repository.countByOrganisationIdAndRoleAndStatus(
                    ORGANISATION_ID, MemberRole.ADMIN, MemberStatus.ACTIVE)).thenReturn(2L);

            service.changeRole(command(id, "MEMBER"));

            verify(repository).save(any());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should reject an unknown role")
        void rejectsUnknownRole() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.changeRole(command(MemberId.generate(), "BOSS")));
        }

        @Test
        @DisplayName("should reject an unknown member")
        void rejectsUnknownMember() {
            MemberId id = MemberId.generate();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(MemberNotFoundException.class, () -> service.changeRole(command(id, "MANAGER")));
        }

        @Test
        @DisplayName("should reject a member from another organisation")
        void rejectsOrganisationMismatch() {
            MemberId id = MemberId.generate();
            Member other = Member.reconstitute(
                    id, OrganisationId.generate(), UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
            when(repository.findById(id)).thenReturn(Optional.of(other));

            assertThrows(OrganisationMismatchException.class, () -> service.changeRole(command(id, "MANAGER")));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null repository")
        void rejectsNullRepository() {
            assertThrows(NullPointerException.class, () -> new ChangeMemberRoleService(null, publisher, CLOCK));
        }

        @Test
        @DisplayName("should reject a null publisher")
        void rejectsNullPublisher() {
            assertThrows(NullPointerException.class, () -> new ChangeMemberRoleService(repository, null, CLOCK));
        }

        @Test
        @DisplayName("should reject a null clock")
        void rejectsNullClock() {
            assertThrows(NullPointerException.class, () -> new ChangeMemberRoleService(repository, publisher, null));
        }
    }
}
