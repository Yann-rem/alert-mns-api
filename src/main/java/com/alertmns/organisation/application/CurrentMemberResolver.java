package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.UserId;

import java.util.Objects;

/**
 * Résout le {@link Member} correspondant à l'utilisateur authentifié courant.
 *
 * <p>Collaborateur d'application partagé : il vit dans le BC Organisation (propriétaire de l'agrégat {@code Member})
 * et est réutilisé par les BC Messaging et Alerting, qui agissent « au nom » du membre courant. Encapsule la
 * traduction utilisateur courant → Member, évitant de la dupliquer (et de dupliquer le couplage vers
 * {@link MemberRepository}) dans chaque service consommateur.</p>
 */
public final class CurrentMemberResolver {

    private final CurrentUserPort currentUserPort;
    private final MemberRepository memberRepository;

    public CurrentMemberResolver(CurrentUserPort currentUserPort, MemberRepository memberRepository) {
        this.currentUserPort = Objects.requireNonNull(currentUserPort, "currentUserPort must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
    }

    /**
     * Retourne le {@link Member} de l'utilisateur authentifié courant.
     *
     * @throws IllegalStateException si aucun utilisateur n'est authentifié, ou s'il n'a pas de Member rattaché
     */
    public Member resolveCurrentMember() {
        UserId currentUserId = currentUserPort.currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"))
                .userId();
        return memberRepository.findByUserId(currentUserId.value())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user has no member: " + currentUserId.value()));
    }
}
