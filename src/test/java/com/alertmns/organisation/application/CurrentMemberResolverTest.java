package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.AuthenticatedUser;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("CurrentMemberResolver")
@ExtendWith(MockitoExtension.class)
class CurrentMemberResolverTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    CurrentUserPort currentUserPort;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    CurrentMemberResolver resolver;

    @Test
    @DisplayName("should resolve the Member of the authenticated user")
    void shouldResolveCurrentMember() {
        UserId userId = UserId.generate();
        Member member = Member.reconstitute(
                MemberId.generate(), ORGANISATION_ID, userId.value(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
        when(currentUserPort.currentUser()).thenReturn(Optional.of(new AuthenticatedUser(userId)));
        when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.of(member));

        assertEquals(member, resolver.resolveCurrentMember());
    }

    @Test
    @DisplayName("should throw when there is no authenticated user")
    void shouldThrowWhenNoCurrentUser() {
        when(currentUserPort.currentUser()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> resolver.resolveCurrentMember());
    }

    @Test
    @DisplayName("should throw when the authenticated user has no member")
    void shouldThrowWhenUserHasNoMember() {
        UserId userId = UserId.generate();
        when(currentUserPort.currentUser()).thenReturn(Optional.of(new AuthenticatedUser(userId)));
        when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> resolver.resolveCurrentMember());
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null currentUserPort")
        void shouldRejectNullCurrentUserPort() {
            assertThrows(NullPointerException.class, () -> new CurrentMemberResolver(null, memberRepository));
        }

        @Test
        @DisplayName("should reject null memberRepository")
        void shouldRejectNullMemberRepository() {
            assertThrows(NullPointerException.class, () -> new CurrentMemberResolver(currentUserPort, null));
        }
    }
}
