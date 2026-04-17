package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ActivateMemberService")
@ExtendWith(MockitoExtension.class)
class ActivateMemberServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID USER_ID = UUID.randomUUID();

    @Mock
    MemberRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    ActivateMemberService service;

    @Nested
    @DisplayName("Activation")
    class Activation {

        MemberId id;
        Member pendingMember;

        @BeforeEach
        void setUp() {
            id = MemberId.generate();

            pendingMember = Member.reconstitute(
                    id,
                    ORGANISATION_ID,
                    USER_ID,
                    MemberRole.MEMBER,
                    MemberStatus.PENDING,
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should activate a member")
        void shouldActivateAMember() {
            when(repository.findById(any())).thenReturn(Optional.of(pendingMember));
            ActivateMemberCommand command = new ActivateMemberCommand(id.value().toString());
            service.activate(command);
            verify(repository).save(any(Member.class));
            verify(publisher).publish(anyList());
        }

        @Test
        @DisplayName("should throw MemberNotFoundException when member not found")
        void shouldThrowMemberNotFoundExceptionWhenMemberNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());
            ActivateMemberCommand command = new ActivateMemberCommand(id.value().toString());
            assertThrows(MemberNotFoundException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalStateException when member is not PENDING")
        void shouldThrowIllegalStateExceptionWhenMemberIsNotPENDING() {
            Member activeMember = Member.reconstitute(
                    pendingMember.id(),
                    pendingMember.organisationId(),
                    pendingMember.userId(),
                    pendingMember.role(),
                    MemberStatus.ACTIVE,
                    pendingMember.joinedAt()
            );

            when(repository.findById(any())).thenReturn(Optional.of(activeMember));
            ActivateMemberCommand command = new ActivateMemberCommand(id.value().toString());
            assertThrows(IllegalStateException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when memberId is not a valid UUID")
        void shouldThrowWhenMemberIdIsInvalid() {
            ActivateMemberCommand command = new ActivateMemberCommand("invalid");
            assertThrows(IllegalArgumentException.class, () -> service.activate(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberService(repository, null));
        }

        @Test
        @DisplayName("should reject null command memberId")
        void shouldRejectNullCommandMemberId() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberCommand(null));
        }
    }
}
