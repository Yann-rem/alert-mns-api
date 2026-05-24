package com.alertmns.organisation.infrastructure.adapter.outgoing.authorities;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberRole;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("MemberUserAuthoritiesAdapter")
@ExtendWith(MockitoExtension.class)
class MemberUserAuthoritiesAdapterTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberUserAuthoritiesAdapter adapter;

    @Nested
    @DisplayName("findAuthorities")
    class FindAuthorities {

        @Test
        @DisplayName("should return ROLE_ADMIN when Member role is ADMIN")
        void shouldReturnRoleAdminWhenMemberRoleIsAdmin() {
            UserId userId = UserId.generate();
            Member adminMember = Member.invite(OrganisationId.generate(), userId.value(), MemberRole.ADMIN);
            when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.of(adminMember));

            List<String> authorities = adapter.findAuthorities(userId);

            assertThat(authorities).containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should return ROLE_MEMBER when Member role is MEMBER")
        void shouldReturnRoleMemberWhenMemberRoleIsMember() {
            UserId userId = UserId.generate();
            Member regularMember = Member.invite(OrganisationId.generate(), userId.value(), MemberRole.MEMBER);
            when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.of(regularMember));

            List<String> authorities = adapter.findAuthorities(userId);

            assertThat(authorities).containsExactly("ROLE_MEMBER");
        }

        @Test
        @DisplayName("should return an empty list when no Member is attached to the user")
        void shouldReturnEmptyListWhenNoMember() {
            UserId userId = UserId.generate();
            when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.empty());

            List<String> authorities = adapter.findAuthorities(userId);

            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("should query the repository with the raw userId UUID")
        void shouldQueryRepositoryWithRawUserId() {
            UserId userId = UserId.generate();
            Member member = Member.invite(OrganisationId.generate(), userId.value(), MemberRole.MEMBER);
            when(memberRepository.findByUserId(userId.value())).thenReturn(Optional.of(member));

            adapter.findAuthorities(userId);
            // Vérifié implicitement par le matcher strict sur findByUserId(userId.value()) : si l'adapter
            // transformait le userId ou passait autre chose, le stub ne matcherait pas et le test échouerait.
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null MemberRepository")
        void shouldRejectNullMemberRepository() {
            assertThrows(NullPointerException.class, () -> new MemberUserAuthoritiesAdapter(null));
        }
    }
}
