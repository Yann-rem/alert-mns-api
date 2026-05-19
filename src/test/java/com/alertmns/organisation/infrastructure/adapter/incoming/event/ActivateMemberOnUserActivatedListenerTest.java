package com.alertmns.organisation.infrastructure.adapter.incoming.event;

import com.alertmns.iam.domain.event.UserActivated;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.port.incoming.ActivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ActivateMemberOnUserActivatedListener")
@ExtendWith(MockitoExtension.class)
class ActivateMemberOnUserActivatedListenerTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    ActivateMemberUseCase activateMemberUseCase;

    @InjectMocks
    ActivateMemberOnUserActivatedListener listener;

    @Nested
    @DisplayName("Cascade")
    class Cascade {

        @Test
        @DisplayName("should activate the Member when UserActivated event is received")
        void shouldActivateMemberWhenUserActivatedEventIsReceived() {
            UserId userId = UserId.generate();
            OrganisationId organisationId = OrganisationId.generate();
            Member member = Member.invite(organisationId, userId.value(), MemberRole.MEMBER);
            when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.of(member));

            listener.onUserActivated(new UserActivated(userId));

            verify(activateMemberUseCase).activate(new ActivateMemberCommand(
                    organisationId.value().toString(),
                    member.id().value().toString()
            ));
        }

        @Test
        @DisplayName("should throw IllegalStateException when no Member is found for the user")
        void shouldThrowWhenNoMemberIsFoundForTheUser() {
            UserId userId = UserId.generate();
            when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> listener.onUserActivated(new UserActivated(userId)));

            verify(activateMemberUseCase, never()).activate(any());
        }

        @Test
        @DisplayName("should look up the Member with the userId from the event")
        void shouldLookUpMemberWithUserIdFromEvent() {
            UserId userId = UserId.generate();
            UUID expectedRawUserId = userId.value();
            Member member = Member.invite(
                    OrganisationId.generate(), expectedRawUserId, MemberRole.MEMBER);
            when(memberRepository.findByUserId(expectedRawUserId)).thenReturn(Optional.of(member));

            listener.onUserActivated(new UserActivated(userId));

            verify(memberRepository).findByUserId(expectedRawUserId);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null MemberRepository")
        void shouldRejectNullMemberRepository() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberOnUserActivatedListener(null, activateMemberUseCase));
        }

        @Test
        @DisplayName("should reject null ActivateMemberUseCase")
        void shouldRejectNullActivateMemberUseCase() {
            assertThrows(NullPointerException.class,
                    () -> new ActivateMemberOnUserActivatedListener(memberRepository, null));
        }
    }
}
