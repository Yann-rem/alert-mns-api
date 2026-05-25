package com.alertmns.identity.domain.port.incoming;

import com.alertmns.identity.domain.port.incoming.command.UpdateProfileCommand;

/**
 * Port entrant représentant le cas d'utilisation de mise à jour du profil d'un utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.identity.application.UpdateProfileService}.</p>
 */
public interface UpdateProfileUseCase {

    /**
     * Met à jour le profil d'un utilisateur.
     *
     * @param command la commande de mise à jour du profil
     */
    void update(UpdateProfileCommand command);
}
