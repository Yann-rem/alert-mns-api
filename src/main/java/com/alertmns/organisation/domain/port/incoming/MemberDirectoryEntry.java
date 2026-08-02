package com.alertmns.organisation.domain.port.incoming;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort.UserSummary;

import java.util.Objects;
import java.util.Optional;

/**
 * Un membre accompagné de l'identité de l'utilisateur correspondant.
 *
 * <p>Résultat de {@link ListMembersUseCase}. L'identité est optionnelle : un membre dont
 * l'utilisateur serait introuvable reste listé (avec {@code user} vide) plutôt que de faire
 * échouer toute la page — l'administration doit pouvoir voir et corriger une telle anomalie.</p>
 */
public record MemberDirectoryEntry(Member member, Optional<UserSummary> user) {

    public MemberDirectoryEntry {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(user, "user must not be null");
    }
}
