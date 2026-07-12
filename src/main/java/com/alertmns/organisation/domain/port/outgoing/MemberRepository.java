package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.shared.OrganisationId;

import java.util.List;
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
     * Recherche le membre rattaché à un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     * @return le membre trouvé, ou vide
     */
    Optional<Member> findByUserId(UUID userId);

    /**
     * Vérifie si un utilisateur est déjà membre d'une organisation.
     *
     * @param organisationId l'identifiant de l'organisation
     * @param userId         l'identifiant de l'utilisateur
     * @return {@code true} si un membre existe déjà pour ce couple
     */
    boolean existsByOrganisationIdAndUserId(OrganisationId organisationId, UUID userId);

    /**
     * Compte les membres d'une organisation ayant un rôle et un statut donnés.
     *
     * <p>Sert l'invariant « au moins un ADMIN actif » (ADR-0013).</p>
     *
     * @param organisationId l'organisation
     * @param role           le rôle recherché
     * @param status         le statut recherché
     * @return le nombre de membres correspondants
     */
    long countByOrganisationIdAndRoleAndStatus(OrganisationId organisationId, MemberRole role, MemberStatus status);

    /**
     * Retourne les {@code userId} de tous les membres d'une organisation.
     *
     * <p>Sert la résolution des destinataires d'une alerte visant toute l'organisation (BC Alerting, via ACL).</p>
     *
     * @param organisationId l'organisation
     * @return les identifiants utilisateur des membres de l'organisation
     */
    List<UUID> findUserIdsByOrganisationId(OrganisationId organisationId);

    /**
     * Retourne les {@code userId} de tous les membres d'un groupe.
     *
     * <p>Sert la résolution des destinataires d'une alerte ciblant un groupe précis (BC Alerting, via ACL).</p>
     *
     * @param groupId le groupe
     * @return les identifiants utilisateur des membres du groupe
     */
    List<UUID> findUserIdsByGroupId(GroupId groupId);
}
