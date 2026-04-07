package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;

/**
 * Port entrant pour la mise à jour du message d'absence d'un utilisateur.
 * Implémenté par UpdateAbsenceMessageService dans la couche applicative.
 */
public interface UpdateAbsenceMessageUseCase {

    void update(UpdateAbsenceMessageCommand command);
}
