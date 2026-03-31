package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.UpdateProfileCommand;

/**
 * Port entrant pour la mise à jour du profil d'un utilisateur.
 * Implémenté par UpdateProfileService dans la couche applicative.
 */
public interface UpdateProfileUseCase {

    void update(UpdateProfileCommand command);
}
