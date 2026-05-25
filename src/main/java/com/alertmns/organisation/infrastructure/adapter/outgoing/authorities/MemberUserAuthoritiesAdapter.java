package com.alertmns.organisation.infrastructure.adapter.outgoing.authorities;

import com.alertmns.identity.domain.port.outgoing.UserAuthoritiesProvider;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.UserId;

import java.util.List;
import java.util.Objects;

/**
 * Adapter résolvant les autorités d'un utilisateur depuis son {@code Member}.
 *
 * <p>Mapping prescrit en mono-tenant :</p>
 * <ul>
 *     <li>{@code Member.role == ADMIN} → {@code ["ROLE_ADMIN"]}</li>
 *     <li>{@code Member.role == MEMBER} → {@code ["ROLE_MEMBER"]}</li>
 *     <li>Pas de {@code Member} → {@code []}</li>
 * </ul>
 */
public final class MemberUserAuthoritiesAdapter implements UserAuthoritiesProvider {

    private final MemberRepository memberRepository;

    public MemberUserAuthoritiesAdapter(MemberRepository memberRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
    }

    @Override
    public List<String> findAuthorities(UserId userId) {
        return memberRepository.findByUserId(userId.value())
                .map(member -> List.of("ROLE_" + member.role().name()))
                .orElse(List.of());
    }
}
