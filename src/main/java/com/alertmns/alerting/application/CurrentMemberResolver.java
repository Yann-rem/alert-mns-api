package com.alertmns.alerting.application;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.UserId;

import java.util.Objects;

/**
 * Résout le {@link Member} correspondant à l'utilisateur authentifié courant, pour les services du BC Alerting.
 *
 * <p><b>Dette assumée</b> : ce collaborateur est un doublon de {@code messaging.application.CurrentMemberResolver}.
 * La duplication garde le BC Alerting auto-porté (pas de couplage vers le BC Messaging). Maintenant que deux BC en
 * ont besoin, l'extraction vers un point unique (BC Organisation, use case entrant « membre courant ») est justifiée
 * — à traiter dans un refactoring dédié.</p>
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
