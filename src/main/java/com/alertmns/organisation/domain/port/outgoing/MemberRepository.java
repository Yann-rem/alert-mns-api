package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.shared.OrganisationId;

import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant pour la persistance des membres d'une organisation.
 */
public interface MemberRepository {

    /**
     * Sauvegarde un membre (création ou mise à jour).
     *
     * @param member le membre à sauvegarder
     */
    void save(Member member);

    /**
     * Recherche un membre par son identifiant.
     *
     * @param id l'identifiant du membre
     * @return le membre trouvé, ou vide
     */
    Optional<Member> findById(MemberId id);

    /**
     * Vérifie si un utilisateur est déjà membre d'une organisation.
     *
     * @param organisationId l'identifiant de l'organisation
     * @param userId         l'identifiant de l'utilisateur
     * @return {@code true} si un membre existe déjà pour ce couple
     */
    boolean existsByOrganisationIdAndUserId(OrganisationId organisationId, UUID userId);
}
