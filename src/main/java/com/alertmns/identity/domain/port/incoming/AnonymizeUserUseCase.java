package com.alertmns.identity.domain.port.incoming;

import com.alertmns.identity.application.AnonymizeUserService;
import com.alertmns.identity.domain.port.incoming.command.AnonymizeUserCommand;

/**
 * Port entrant représentant le cas d'utilisation d'anonymisation d'un utilisateur (droit à l'effacement RGPD,
 * article 17 — doctrine ADR-0017).
 *
 * <p>Implémenté par {@link AnonymizeUserService}.</p>
 */
public interface AnonymizeUserUseCase {

    /**
     * Anonymise un utilisateur : ses données personnelles sont remplacées par des placeholders non-identifiants, son
     * {@code userId} est conservé. Opération idempotente.
     *
     * @param command la commande d'anonymisation
     */
    void anonymize(AnonymizeUserCommand command);
}
