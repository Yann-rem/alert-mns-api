package com.alertmns.organisation.infrastructure.adapter.outgoing.authorities;

import com.alertmns.identity.domain.port.outgoing.UserMembershipProvider;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.UserId;

import java.util.Objects;
import java.util.Optional;

/**
 * Résout l'adhésion d'un utilisateur depuis son {@code Member}.
 *
 * <p>Pendant de {@link MemberUserAuthoritiesAdapter} : celui-ci fournit les autorités de sécurité,
 * celui-là les informations d'adhésion affichables (organisation, rôle, statut).</p>
 */
public final class MemberMembershipAdapter implements UserMembershipProvider {

    private final MemberRepository memberRepository;

    public MemberMembershipAdapter(MemberRepository memberRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
    }

    @Override
    public Optional<Membership> findByUserId(UserId userId) {
        return memberRepository.findByUserId(userId.value())
                .map(member -> new Membership(
                        member.organisationId().value(),
                        member.role().name(),
                        member.status().name()));
    }
}
