package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.MembershipInvitation;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des invitations à rejoindre une organisation.
 */
public interface MembershipInvitationRepository {

    /**
     * Sauvegarde une invitation (création ou mise à jour).
     *
     * @param invitation l'invitation à sauvegarder
     */
    void save(MembershipInvitation invitation);

    /**
     * Recherche une invitation par son identifiant.
     *
     * @param id l'identifiant de l'invitation
     * @return l'invitation trouvée, ou vide
     */
    Optional<MembershipInvitation> findById(MembershipInvitationId id);

    /**
     * Recherche l'unique invitation {@code PENDING} pour un email donné.
     *
     * @param email l'email recherché
     * @return l'invitation PENDING trouvée, ou vide
     */
    Optional<MembershipInvitation> findPendingByEmail(Email email);

    /**
     * Vérifie si une invitation {@code PENDING} existe pour cet email.
     *
     * @param email l'email recherché
     * @return {@code true} si l'invitation PENDING existe déjà
     */
    boolean existsPendingByEmail(Email email);
}
