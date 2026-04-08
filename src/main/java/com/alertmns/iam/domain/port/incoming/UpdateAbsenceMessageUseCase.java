package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;

/**
 * Port entrant représentant le cas d'utilisation de mise à jour du message d'absence d'un utilisateur.
 *
 * <p>Implémenté par {@link com.alertmns.iam.application.UpdateAbsenceMessageService}.</p>
 */
public interface UpdateAbsenceMessageUseCase {

    /**
     * Met à jour le message d'absence d'un utilisateur.
     *
     * @param command la commande de mise à jour du message d'absence
     */
    void update(UpdateAbsenceMessageCommand command);
}
